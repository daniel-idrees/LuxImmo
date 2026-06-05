# Architecture Layers in Detail

> Use when writing domain models, use cases, repositories, DTOs, DAOs, or mappers — or building a ViewModel and its State / Action / Effect contract.

> Copy-paste skeletons for the code referenced below (`Result`, use case, `runSuspendCatching`, the network DTO + data source + Retrofit API/client, offline-first repository, and the MVI contracts + `MviViewModel` base) are in [`../template.md`](../template.md).

## Writing style

Cross-cutting habits that keep the codebase consistent no matter who — or what — writes it:

- **Comments explain *why*, not *what*** — non-obvious flow behavior, ordering, or sync rules. Match the surrounding file's comment density.

> **Guardrail:** mirror the nearest existing example — an existing feature, repository, or DI module — for structure and naming before inventing a new shape. A new pattern should be a deliberate, documented decision, not an accident; consistency is what lets anyone (or any AI agent) extend the code without surprises.

## The layers at a glance

Three layers, with dependencies pointing **inward** toward a pure-Kotlin core. The domain layer is the center and knows nothing about Android, the network, or the database:

```
   ┌────────────────────────────────────────────────────────┐
   │  UI        feature modules: Jetpack Compose + MVI        │
   │            ViewModels. Renders state, emits actions.     │
   └─────────────────────────┬──────────────────────────────┘
                             │ depends on
                             ▼
   ┌────────────────────────────────────────────────────────┐
   │  DOMAIN    pure Kotlin: models, use cases, repository    │
   │            INTERFACES, Result type. No Android at all.   │
   └─────────────────────────▲──────────────────────────────┘
                             │ implements
   ┌─────────────────────────┴──────────────────────────────┐
   │  DATA      repository IMPLEMENTATIONS (offline-first):   │
   │            network + database → single source of truth.  │
   └────────────────────────────────────────────────────────┘

   ui → domain ← data   (both UI and data depend on domain; domain depends on neither)
```

Reads flow out of the **database** (the single source of truth); the **network** only refreshes the cache. The UI is a passive function of immutable state, updated through a strict one-way loop (MVI).

> **Guardrail:** respect the dependency direction. A feature must never depend on `:core:data` / `:core:network` / `:core:database`, nor reach for a DAO or network data source directly — if it seems to need the database or network, add a use case / repository method instead. In a single-module project the build can't enforce this, so it's kept by convention (see the single-module variant below).

## 4.1 Domain (`:core:domain`) — pure Kotlin

- **Models**: plain immutable data classes / value types. No annotations from Room, Retrofit, or Compose.
- **Repository interfaces**: describe *what* data is available, not *how* it's fetched. Reads return `Flow<T>`; one-shot writes/refreshes return the domain `Result<T>`. Name them `<Thing>Repository`.
- **Use cases**: one responsibility each, constructor-injected, expose `operator fun invoke(...)`. Keep them thin — they orchestrate repositories; they don't hold state. Name them `<Verb><Noun>UseCase`. *(Use case skeleton in [`../template.md`](../template.md).)*
- **Result type**: a domain-owned sealed result, distinct from Kotlin's `Result`, with a typed error model. **Why:** the UI can exhaustively branch on *typed* failures (offline vs. unknown) instead of inspecting raw exceptions. *(`Result` / `AppError` skeleton in [`../template.md`](../template.md).)*

> **Guardrail:** there is exactly **one** result type. Reuse the domain `Result` + `AppError` everywhere; never introduce a parallel `Either` / `Outcome` / second result type alongside it.

## 4.2 Data (`:core:data`) — offline-first

- Repository **implementations** are `internal` and bound to their interface via Hilt `@Binds`. Name them with an intent-revealing prefix — `OfflineFirst<Thing>Repository` (or similar).
- **Reads come only from the database** (single source of truth). **Refreshes** hit the network, then write to the DB; the DB `Flow` propagates changes to the UI automatically.
- **Mappers** are extension functions kept in the data layer, one direction per function, with intention-revealing names:
  - `NetworkDto.asEntity()` — network → database
  - `Entity.asExternalModel()` — database → domain
  - `to<Layer>Model()` for any other cross-layer conversion
- Wrap network calls so cancellation is never swallowed and errors become typed results, then `.fold(...)` and map the throwable to your domain `AppError`. *(`runSuspendCatching` and the offline-first repository skeleton are in [`../template.md`](../template.md).)*

## 4.3 Network (`:core:network`)

**Stack: Retrofit** (HTTP) + **OkHttp** (client + logging interceptor) + **`kotlinx.serialization`** (JSON).

- DTOs are `@Serializable` data classes mirroring the API; never leak them past the data layer.
- Define a **data-source interface** (`<Thing>NetworkDataSource`) and a Retrofit-backed implementation, so the network can be faked in tests. Choose the Retrofit return shape per endpoint:
  - **Direct object** (e.g. `suspend fun get(): Dto`) — Retrofit throws `HttpException` on any non-2xx; use when every error should just propagate.
  - **`Response<T>`** (e.g. `suspend fun get(id): Response<Dto?>`) — use when the implementation must read the status code, e.g. map 403/404 to `null` instead of throwing. *(Skeleton in [`../template.md`](../template.md).)*
- Configure JSON with `ignoreUnknownKeys = true`.
- Gate HTTP logging on `BuildConfig.DEBUG`.
- Read `BASE_URL` and secrets from `BuildConfig`/build config fields, never hardcoded in source.

## 4.4 Database (`:core:database`)

**Stack: Room** (with **KSP** codegen).

- Room entities (`@Entity`), DAOs (`@Dao`) exposing `Flow` for reads and `suspend` for writes, and the `RoomDatabase`.
- Provide upsert/replace operations for sync (`replaceAll`, `upsert`, `deleteById`).
- Generated code via **KSP**. Provide the DB and DAOs through Hilt modules.

## Network-only (no offline-first) variant

> Applies only when the skill-router preference `offline_first` is **`no`**. When it is `yes` (the default), ignore this section and use §4.2 + §4.4 as written.

When offline-first is disabled, the app fetches straight from the network with no local persistence. The domain (§4.1) and network (§4.3) layers are unchanged; the database and the offline-first machinery are removed:

- **Skip `:core:database` entirely** — no Room, no `@Entity`, no `@Dao`, no `RoomDatabase`, no KSP-for-Room, and no Room convention plugin. (Multi-module: don't create the `:core:database` module and drop `:core:data`'s dependency on it. Single-module: omit the `data/database/` package.)
- **Repository implementation depends only on the network data source** (no DAO). Drop the `OfflineFirst` prefix — name it for what it now is, e.g. `Network<Thing>Repository` or `Default<Thing>Repository`. Still `internal`, still bound to its interface via Hilt `@Binds`.
- **Reads hit the network directly.** There is no database `Flow` acting as a single source of truth, so a read is a one-shot `suspend fun` returning the domain `Result<T>` (network fetched, mapped, wrapped via `runSuspendCatching` + `.fold`). There is no separate `refresh()` — every read goes to the network — and no cache to write back to.
- **Mappers collapse to one hop:** only `NetworkDto.asExternalModel()` (network → domain). There are no entity mappers (`asEntity` / `Entity.asExternalModel`).
- **Repository interface (§4.1) changes shape accordingly:** reads return `suspend fun get<Thing>s(): Result<List<<Thing>>>` instead of `Flow` + `refresh()`. The use case and ViewModel call it the same way they would any one-shot `Result`-returning operation (the ViewModel triggers the load on `Init` and again on pull-to-refresh; there is no DB flow that auto-updates the list).

```kotlin
// Domain repository interface (:core:domain) — one-shot reads, no Flow/refresh split
interface <Thing>Repository {
    suspend fun get<Thing>s(): Result<List<<Thing>>>
    suspend fun get<Thing>(id: Int): Result<<Thing>?>
}

// Network-only implementation (:core:data) — no DAO, no cache, DTO → domain directly
internal class Network<Thing>Repository @Inject constructor(
    private val network: <Thing>NetworkDataSource,
) : <Thing>Repository {

    override suspend fun get<Thing>s(): Result<List<<Thing>>> =
        runSuspendCatching { network.get() }.fold(
            onSuccess = { dto -> Result.Success(dto.items.map(Dto::asExternalModel)) },
            onFailure = { it.toErrorResult() },
        )

    override suspend fun get<Thing>(id: Int): Result<<Thing>?> =
        runSuspendCatching { network.get<Thing>(id) }.fold(
            onSuccess = { dto -> Result.Success(dto?.asExternalModel()) },
            onFailure = { it.toErrorResult() },
        )
}
```

> **Guardrail:** don't mix the two modes. If `offline_first` is `no`, there must be no Room/DAO/entity code and no `OfflineFirst*` repository; if it's `yes`, reads come from the DB and the network only refreshes the cache (§4.2). Switching modes is a deliberate, documented change to `.preferences.md`, not a per-file choice.

## 4.5 UI core (`:core:ui`)

Houses the MVI base classes, shared stateless composables (loading view, error view, app bar, async image), UI-level models, the `Navigator`, and the `ResourceProvider`.

> The UI layer is Jetpack Compose throughout. How a screen's ViewModel and its State / Action / Effect contract are built is covered in §5 below (*The UI loop (MVI)*); how the composables, previews, and test tags are written is outside this skill's scope (it follows the standard stateful-entry-point + stateless-content Compose conventions).

## Single-module variant

In a single-module project the layers are **packages**, not Gradle modules — the code is identical; only placement and visibility change:

| Multi-module | Single-module package |
|---|---|
| `:core:domain` | `domain/` |
| `:core:data` | `data/` |
| `:core:network` | `data/network/` |
| `:core:database` | `data/database/` |
| `:core:ui` | `ui/` |

The same dependency direction (`ui → domain ← data`) and offline-first rules apply, but they're kept by **convention** (optionally a lint / Konsist rule) rather than enforced by the build. Because everything is in one module, `internal` no longer hides a type from other layers — lean on package structure and discipline.

## The UI loop (MVI)

The UI layer is Jetpack Compose driven by a strict one-way loop: the user emits an **Action**, the ViewModel reduces it into immutable **State**, and the screen redraws from that state; one-time events go out as **Effects**. Because there is only one path for changes, screen behavior is easy to follow, reproduce, and fix. Use this when building a ViewModel and its State / Action / Effect contract. *(Copy-paste skeletons — the contracts, the `MviViewModel` base, and a screen's State/Action/Effect + ViewModel — are in [`../template.md`](../template.md); a filled-in example is in [`../examples/articles-list-mvi.md`](../examples/articles-list-mvi.md).)*

> MVI is the UI *pattern*; the composables that render the state are agnostic to it (they work equally with MVVM) and are outside this skill's scope.

## 5.1 Contracts (in `:core:ui`)

Three marker interfaces define the shape of every screen's contract: an immutable `ViewState` (a data class), a `ViewAction` (a sealed interface of user intents), and a `ViewSideEffect` (a sealed interface of one-time events).

## 5.2 Base ViewModel

A generic `MviViewModel` base manages the `StateFlow` of state, a buffered action `SharedFlow`, and a `Channel`-backed effect `Flow`. Key behaviors:

- State updates **only** through `setState { copy(...) }` — never mutate, always reduce.
- One-time events go through `setEffect` (delivered via `Channel`/`receiveAsFlow`) so they survive config changes and are not replayed.
- Actions flow in through `setAction` and are dispatched in `handleAction`.

## 5.3 A feature's MVI contract

Each screen owns four files (or one cohesive set): its `<Screen>UiState`, `<Screen>UiAction`, `<Screen>UiEffect`, and `<Screen>ViewModel`.

**Conventions captured in the templates:**

- ViewModels and their state/action/effect types are `internal` to the feature module.
- State is an **immutable** `data class` — `val`s with sensible defaults, annotated `@Immutable`. Model `Action`/`Effect` (and the domain `Result`, §4.1) as `sealed interface`/`sealed class` so every `when` is exhaustive with **no `else`** branch.
- ViewModels never touch Android `Context`, string resources directly, or `Dispatchers.X` literals — they take a `ResourceProvider` and an injected dispatcher.
- Heavy transformation (mapping/sorting) runs on the injected `@DefaultDispatcher` via `flowOn`.
- Use `combine`, `distinctUntilChanged`, `shareIn(WhileSubscribed(5000), replay = 1)` to merge sources and avoid duplicate upstream work.
- An explicit `Init` action triggers first load (paired with `OneTimeLaunchedEffect` in the UI).

> **Guardrail:** a ViewModel never touches Android `Context`, `Dispatchers.X` literals, or user-facing string literals — inject `ResourceProvider` and a qualified dispatcher instead. Reuse the shared `MviViewModel` base rather than hand-rolling a second state/effect mechanism. (Before/after fixes for both are in [`../examples/articles-list-mvi.md`](../examples/articles-list-mvi.md).)

## MVI in a single module

The pattern is identical. The contracts and the `MviViewModel` base live in a `ui/` package instead of `:core:ui`; a screen's `State` / `Action` / `Effect` + ViewModel live in that feature's `feature/<name>/` package. With one module, `internal` no longer scopes a type to a single feature, so keep each screen's MVI types together in its package and rely on that boundary.

## Adding a new feature end to end

> Use when adding a brand-new feature end to end. Follow start to finish.

A feature cuts across every layer above plus the UI concerns (MVI, Compose, navigation, and tests). Follow the same recipe each time so the structure stays consistent and nothing is left half-done:

1. **Register the feature module** — `include(":feature:<name>")` in `settings.gradle.kts` and add its `build.gradle.kts` (check whether the project uses convention plugins).
2. **Wire navigation** — add `NavKey`(s) and the feature's `entry` provider; register it in `:app`.
3. **Domain slice** (`:core:domain`) — model(s), repository interface, and use case(s) (§4.1).
4. **Data slice** — DTO + network data source (`:core:network`, §4.3), entity + DAO (`:core:database`, §4.4), and the offline-first repository implementation + mappers + Hilt `@Binds` (`:core:data`, §4.2). *(If the skill-router preference `offline_first` is `no`, skip the `:core:database`/entity/DAO work and build the network-only repository instead — see [Network-only (no offline-first) variant](#network-only-no-offline-first-variant).)*
5. **UI** — State / Action / Effect, the `@HiltViewModel` ViewModel, the stateful + stateless composables, and the UI model + mapper (the ViewModel/MVI contract is covered in §5 above; the composables follow the standard stateful-entry-point + stateless-content Compose conventions).
6. **Tests** — a ViewModel unit test (fakes + Turbine) and a Compose UI test driven by test tags.

A file-by-file worked example is in [`../examples/articles-feature-multi-module-walkthrough.md`](../examples/articles-feature-multi-module-walkthrough.md).

### Single-module variant

Same feature, packages instead of modules (see the layer table above for where each slice lands):

1. Create a `feature/<name>/` package — no `settings.gradle.kts` include, no per-feature `build.gradle.kts` (steps 1 above don't apply).
2. Add `NavKey`(s) and the feature's `entry` provider in the same package; register it in the app's nav host.
3. Steps 3–6 are identical, but the slices live in packages (`domain/…`, `data/…`, `feature/<name>/…`) rather than separate modules.

The single-module walkthrough is in [`../examples/articles-feature-single-module-walkthrough.md`](../examples/articles-feature-single-module-walkthrough.md).

### Feature guardrails

> **Guardrail:** a feature must not add cross-feature dependencies or reach into the data layer.
> - **Multi-module:** the feature depends only on `:core:domain`, `:core:ui`, `:core:common` — never on `:core:data` / `:core:network` / `:core:database`, and never on another feature. The module graph enforces this.
> - **Single-module:** the `feature/<name>` package uses only the `domain` and `ui` packages — never `data` / `network` / `database` directly, and never another feature's package. The build can't enforce this, so the discipline lives in convention and code review (optionally a lint / Konsist rule).
