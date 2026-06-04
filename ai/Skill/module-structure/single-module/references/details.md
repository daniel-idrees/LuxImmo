# Single-Module Structure

> Use when structuring a small app, prototype, or sample without multi-module overhead.

The same Clean Architecture (`ui → domain ← data`, offline-first, MVI) organized as **packages inside one Gradle module** instead of many modules. Layer boundaries are kept by **convention** (and optionally lint / Konsist rules) rather than enforced by the build system.

## Package layout

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

## Dependency rules (by convention)

- `feature/*` packages use `domain` and `ui` — not `data` / `network` / `database` directly.
- `domain` has no Android dependencies and depends on nothing else internal.
- `data` depends on `domain`, `network`, `database`.
- Only the `app` / `di` wiring knows about every package.

**Why:** the layering, single-source-of-truth, and MVI benefits are identical; you simply trade build-enforced isolation for a much simpler build setup.

## Trade-offs vs. many modules

- ✅ Far less Gradle ceremony; fastest to start; ideal for prototypes, samples, and small apps.
- ⚠️ Boundaries are **not** compiler-enforced — a feature *can* reach into `data` unless a lint / Konsist rule forbids it. Discipline and code review replace the module graph.
- ⚠️ `internal` only hides things from outside the module; within one module everything `internal` is visible, so it's a weaker boundary signal.
- ⚠️ No per-module incremental or parallel builds; the whole module recompiles.

## Visibility

Default to `internal` for everything that isn't deliberate public API — but note the caveat above: with one module, `internal` only hides a type from *outside* the module, so it can't mark something "feature-private." Keep each feature in its own package and rely on that boundary; if you want it enforced, add a lint / Konsist rule. Public API (domain models, repository interfaces, use cases) stays `public` as usual.

## When to graduate to many modules

Split into modules once build times grow, multiple people or teams work in parallel, you want compiler-enforced feature isolation, or the app accumulates many features.
