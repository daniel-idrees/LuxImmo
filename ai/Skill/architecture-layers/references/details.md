# Architecture Layers in Detail

> Use when writing domain models, use cases, repositories, DTOs, DAOs, or mappers.

> Copy-paste skeletons for the code referenced below (`Result`, use case, `runSuspendCatching`, offline-first repository) are in [`../template.md`](../template.md).

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

- DTOs are `@Serializable` data classes mirroring the API; never leak them past the data layer.
- Define a **data-source interface** (`<Thing>NetworkDataSource`) and a Retrofit-backed implementation, so the network can be faked in tests.
- Configure JSON with `ignoreUnknownKeys = true`.
- Gate HTTP logging on `BuildConfig.DEBUG`.
- Read `BASE_URL` and secrets from `BuildConfig`/build config fields, never hardcoded in source.

## 4.4 Database (`:core:database`)

- Room entities (`@Entity`), DAOs (`@Dao`) exposing `Flow` for reads and `suspend` for writes, and the `RoomDatabase`.
- Provide upsert/replace operations for sync (`replaceAll`, `upsert`, `deleteById`).
- Generated code via **KSP**. Provide the DB and DAOs through Hilt modules.

## 4.5 UI core (`:core:ui`)

Houses the MVI base classes, shared stateless composables (loading view, error view, app bar, async image), UI-level models, the `Navigator`, and the `ResourceProvider`.

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
