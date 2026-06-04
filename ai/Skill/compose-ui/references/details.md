# Compose UI Conventions

> Use when writing screens, composables, previews, or UI test tags.

- **Two composables per screen**: a stateful entry point that takes the `ViewModel` and collects state, and a `@VisibleForTesting` stateless one that takes `state` + `onAction: (Action) -> Unit`. Hoist all state; the stateless version is what previews and UI tests drive. *(Copy-paste skeleton in [`../template.md`](../template.md).)*
- Collect state with **`collectAsStateWithLifecycle()`** (not `collectAsState()`).
- Collect effects once in a `LaunchedEffect`, branching to navigation/snackbar handlers.
- Use **`OneTimeLaunchedEffect`** (a `rememberSaveable`-guarded `LaunchedEffect`) for one-shot init that must survive config changes.
- Drive spacing/sizing from **design-system tokens** (`SPACING_SMALL`, `SPACING_MEDIUM`, …) — no magic `dp` numbers scattered in features.
- Pull colors/typography from `MaterialTheme.colorScheme` / `MaterialTheme.typography`, themed by the design system.
- Resolve user-facing strings with **`stringResource`** / **`pluralStringResource`** — never build them by concatenation. (ViewModels use `ResourceProvider` instead; see the `mvi-pattern` skill.)
- Add **`testTag`s** to every element a UI test needs (`testTag("item_${id}")`).
- Provide **`@Preview`s** for every meaningful state (content, loading, empty, error). Use a custom multi-preview annotation (e.g. `@LightDarkPreviews`) to render light + dark at once.
- Keep composables **stateless and side-effect-free** except for the thin stateful wrapper.

> **Rule:** no business logic in composables or in the design system. Composables render state and emit actions; that's it.

> **Guardrail:** every new screen ships with the full set — a stateful + stateless composable pair, `@Preview`s for *every* state (content, loading, empty, error), `testTag`s on the nodes tests need, Turbine unit tests for its ViewModel (see the `unit-testing` skill), and typed error handling that distinguishes offline from unknown failures.

## Single-module variant

The composable conventions are identical. Design-system tokens (`SPACING_*`, theme colors/typography) come from a `designsystem` / `ui` package rather than a `:core:designsystem` module, and `@VisibleForTesting internal` types are visible module-wide — so keep each screen's composables in its own `feature/<name>/` package.
