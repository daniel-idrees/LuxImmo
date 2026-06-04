# Multi-Module Structure

> Use when adding a module, wiring dependencies, or checking the allowed dependency direction.

```
:app                      # Entry point: Application, MainActivity, root navigation host. Wires features together.
:core
  :core:common            # Pure utilities, coroutine dispatcher qualifiers + module. No Android UI.
  :core:domain            # Models, repository INTERFACES, use cases, Result type. Pure Kotlin, no Android deps.
  :core:data              # Repository IMPLEMENTATIONS, model mappers, combines network + database.
  :core:network           # DTOs, data-source interfaces + Retrofit impl, JSON, networking DI.
  :core:database          # Room database, entities, DAOs, database DI.
  :core:designsystem      # Theme, color, typography, spacing/sizing tokens, icons, atomic components. UI only.
  :core:ui                # MVI base classes, shared composables, ResourceProvider, Navigator, UI models.
  :core:testing           # Shared test fakes, test data, JUnit rules. Consumed via testImplementation.
:feature
  :feature:<name>         # One self-contained feature (UI + ViewModels + feature navigation). Depends only on :core:*.
:benchmark                # Macrobenchmark module (com.android.test) for startup/baseline profiles.
```

## Dependency rules (enforced, not suggested)

- `:feature:*` depends on `:core:domain`, `:core:ui`, `:core:common` — **never** on another feature, **never** on `:core:data` / `:core:network` / `:core:database`.
- `:core:domain` depends on **nothing** internal and has no Android dependencies.
- `:core:data` depends on `:core:domain`, `:core:network`, `:core:database`.
- Only `:app` knows about every layer and assembles the graph.
- `:core:ui` may depend on `:core:designsystem`; never the reverse.

**Why:** features can be built, tested, and reasoned about in isolation; swapping a data source never touches feature code; and the build system, not discipline, keeps the boundaries.

> **Rule:** no business logic in composables or in the design system. Composables render state and emit actions; that's it.

## Visibility

Default to `internal` for everything module-private — ViewModels, repository implementations, mappers, DI bindings. Mark a type `public` **only** when it crosses a module boundary (domain models, repository interfaces, use cases, shared `:core:ui` types). In multi-module projects the compiler enforces this: `internal` keeps a type out of every other module's reach.
