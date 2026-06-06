# Adding a New Feature End to End

> Use when adding a brand-new feature end to end. Follow start to finish.

> This recipe cuts across the layers ([`layers.md`](layers.md)) and the MVI ViewModel loop ([`mvi.md`](mvi.md)); copy-paste skeletons are in [`../template.md`](../template.md).

A feature cuts across every layer plus the UI concerns (MVI, Compose, navigation, and tests). Follow the same recipe each time so the structure stays consistent and nothing is left half-done:

1. **Register the feature module** — `include(":feature:<name>")` in `settings.gradle.kts` and add its `build.gradle.kts` (check whether the project uses convention plugins).
2. **Wire navigation** — add `NavKey`(s) and the feature's `entry` provider; register it in `:app`.
3. **Domain slice** (`:core:domain`) — model(s), repository interface, and use case(s) (§4.1 in [`layers.md`](layers.md)).
4. **Data slice** — DTO + network data source (`:core:network`, §4.3 in [`layers.md`](layers.md)), entity + DAO (`:core:database`, §4.4 in [`layers.md`](layers.md)), and the offline-first repository implementation + mappers + Hilt `@Binds` (`:core:data`, §4.2 in [`layers.md`](layers.md)). *(If the app is **not** offline-first, skip the `:core:database`/entity/DAO work and build the network-only repository instead — see [`layers.md`](layers.md#network-only-no-offline-first-variant).)*
5. **UI** — State / Action / Effect, the `@HiltViewModel` ViewModel, the stateful + stateless composables, and the UI model + mapper (the ViewModel/MVI contract is covered in §5, [`mvi.md`](mvi.md); the composables follow the standard stateful-entry-point + stateless-content Compose conventions).
6. **Tests** — a ViewModel unit test (fakes + Turbine) and a Compose UI test driven by test tags.

A file-by-file worked example is in [`../examples/articles-feature-multi-module-walkthrough.md`](../examples/articles-feature-multi-module-walkthrough.md).

## Single-module variant

Same feature, packages instead of modules (see the layer table in [`layers.md`](layers.md) for where each slice lands):

1. Create a `feature/<name>/` package — no `settings.gradle.kts` include, no per-feature `build.gradle.kts` (steps 1 above don't apply).
2. Add `NavKey`(s) and the feature's `entry` provider in the same package; register it in the app's nav host.
3. Steps 3–6 are identical, but the slices live in packages (`domain/…`, `data/…`, `feature/<name>/…`) rather than separate modules.

The single-module walkthrough is in [`../examples/articles-feature-single-module-walkthrough.md`](../examples/articles-feature-single-module-walkthrough.md).

## Feature guardrails

> **Guardrail:** a feature must not add cross-feature dependencies or reach into the data layer.
> - **Multi-module:** the feature depends only on `:core:domain`, `:core:ui`, `:core:common` — never on `:core:data` / `:core:network` / `:core:database`, and never on another feature. The module graph enforces this.
> - **Single-module:** the `feature/<name>` package uses only the `domain` and `ui` packages — never `data` / `network` / `database` directly, and never another feature's package. The build can't enforce this, so the discipline lives in convention and code review (optionally a lint / Konsist rule).
