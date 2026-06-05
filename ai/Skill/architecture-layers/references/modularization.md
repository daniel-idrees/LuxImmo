# Modularization — Multi-Module vs. Single-Module

> Use when adding a module, wiring dependencies, checking the allowed dependency direction, or deciding how to structure a project. Which approach applies is set once by the skill-router preference `module_structure`.

Two ways to organize the same Clean Architecture (`ui → domain ← data`, offline-first, MVI):

- **Multi-module** — the app is split into many small Gradle modules, by layer (`:core:*`) and by feature (`:feature:*`), with the build system itself enforcing which module may depend on which. The boundaries are strong and compiler-checked, and each part can be built, tested, and reasoned about on its own. This is the default for apps expected to grow.
- **Single-module** — the same layered, clean separation organized as **packages inside one Gradle module** instead of many modules. It is lighter to set up and a good fit for small apps, prototypes, and samples. The trade-off is that the boundaries between layers are kept by **convention** (and optional lint / Konsist rules) rather than enforced by the build system.

---

## Multi-Module Structure

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

### Dependency rules (enforced, not suggested)

- `:feature:*` depends on `:core:domain`, `:core:ui`, `:core:common` — **never** on another feature, **never** on `:core:data` / `:core:network` / `:core:database`.
- `:core:domain` depends on **nothing** internal and has no Android dependencies.
- `:core:data` depends on `:core:domain`, `:core:network`, `:core:database`.
- Only `:app` knows about every layer and assembles the graph.
- `:core:ui` may depend on `:core:designsystem`; never the reverse.

> **Offline-first toggle:** `:core:database` exists only when the skill-router preference `offline_first` is `yes`. When it is `no`, omit the `:core:database` module entirely and drop it from `:core:data`'s dependencies — `:core:data` then depends only on `:core:domain` and `:core:network`. See [`details.md`](details.md) → *Network-only (no offline-first) variant*.

**Why:** features can be built, tested, and reasoned about in isolation; swapping a data source never touches feature code; and the build system, not discipline, keeps the boundaries.

> **Rule:** no business logic in composables or in the design system. Composables render state and emit actions; that's it.

### Visibility

Default to `internal` for everything module-private — ViewModels, repository implementations, mappers, DI bindings. Mark a type `public` **only** when it crosses a module boundary (domain models, repository interfaces, use cases, shared `:core:ui` types). In multi-module projects the compiler enforces this: `internal` keeps a type out of every other module's reach.

---

## Single-Module Structure

> Use when structuring a small app, prototype, or sample without multi-module overhead.

The same Clean Architecture (`ui → domain ← data`, offline-first, MVI) organized as **packages inside one Gradle module** instead of many modules. Layer boundaries are kept by **convention** (and optionally lint / Konsist rules) rather than enforced by the build system.

### Package layout

```
com.example.app
├─ di/                 # Hilt modules, coroutine dispatcher qualifiers
├─ domain/             # models, repository INTERFACES, use cases, Result type (no Android)
├─ data/               # repository IMPLEMENTATIONS, mappers
│   ├─ network/        # DTOs, data-source interfaces + Retrofit impl, JSON
│   └─ database/       # Room database, entities, DAOs
├─ ui/                 # MVI base classes, design system, shared composables, ResourceProvider, Navigator
└─ feature/
    └─ <name>/         # one feature: screen composables + ViewModel + feature navigation
```

### Dependency rules (by convention)

- `feature/*` packages use `domain` and `ui` — not `data` / `network` / `database` directly.
- `domain` has no Android dependencies and depends on nothing else internal.
- `data` depends on `domain`, `network`, `database`.
- Only the `app` / `di` wiring knows about every package.

> **Offline-first toggle:** the `data/database/` package (Room) exists only when the skill-router preference `offline_first` is `yes`. When it is `no`, omit it and use the network-only repository (`data` then uses only `domain` and `network`). See [`details.md`](details.md) → *Network-only (no offline-first) variant*.

**Why:** the layering, single-source-of-truth, and MVI benefits are identical; you simply trade build-enforced isolation for a much simpler build setup.

> **Rule:** no business logic in composables or in the design system. Composables render state and emit actions; that's it.

### Trade-offs vs. many modules

- ✅ Far less Gradle ceremony; fastest to start; ideal for prototypes, samples, and small apps.
- ⚠️ Boundaries are **not** compiler-enforced — a feature *can* reach into `data` unless a lint / Konsist rule forbids it. Discipline and code review replace the module graph.
- ⚠️ `internal` only hides things from outside the module; within one module everything `internal` is visible, so it's a weaker boundary signal.
- ⚠️ No per-module incremental or parallel builds; the whole module recompiles.

### Visibility

Default to `internal` for everything that isn't deliberate public API — but note the caveat above: with one module, `internal` only hides a type from *outside* the module, so it can't mark something "feature-private." Keep each feature in its own package and rely on that boundary; if you want it enforced, add a lint / Konsist rule. Public API (domain models, repository interfaces, use cases) stays `public` as usual.

### When to graduate to many modules

Split into modules once build times grow, multiple people or teams work in parallel, you want compiler-enforced feature isolation, or the app accumulates many features.
