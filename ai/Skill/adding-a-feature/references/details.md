
# Adding a New Feature (Checklist)

> Use when adding a brand-new feature end to end. Follow start to finish.

1. Register the new feature module — `include(":feature:<name>")` in `settings.gradle.kts` and add its `build.gradle.kts`. (check whether the project uses convention plugins).
2. Add `NavKey`(s) and the feature's `entry` provider; register it in `:app`.

## Single-module variant

Same feature, packages instead of modules:

1. Create a `feature/<name>/` package — no `settings.gradle.kts` include, no per-feature `build.gradle.kts`.
2. Add `NavKey`(s) and the feature's `entry` provider in the same package; register it in the app's nav host.

## Guardrails

A feature must not add cross-feature dependencies or reach into the data layer:

- **Multi-module:** the feature depends only on `:core:domain`, `:core:ui`, `:core:common` — never on `:core:data` / `:core:network` / `:core:database`, and never on another feature. The module graph enforces this.
- **Single-module:** the `feature/<name>` package uses only the `domain` and `ui` packages — never `data` / `network` / `database` directly, and never another feature's package. The build system can't enforce this, so the discipline lives in convention and code review (optionally a lint / Konsist rule).
