# Architecture Layers in Detail

> Use when writing domain models, use cases, repositories, DTOs, DAOs, or mappers.

> Copy-paste skeletons for the code referenced below (`Result`, use case, `runSuspendCatching`, the network DTO + data source + Retrofit API/client, offline-first repository) are in [`../template.md`](../template.md). The MVI ViewModel pattern is in [`mvi.md`](mvi.md); the end-to-end "adding a feature" checklist is in [`adding-a-feature.md`](adding-a-feature.md).

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

> The UI layer is Jetpack Compose throughout. How a screen's ViewModel and its State / Action / Effect contract are built is covered in §5 ([`mvi.md`](mvi.md)); how the composables, previews, and test tags are written is outside this skill's scope (it follows the standard stateful-entry-point + stateless-content Compose conventions).

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
