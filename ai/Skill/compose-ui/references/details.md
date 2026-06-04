# Compose UI Conventions

> Use when writing screens, composables, previews, or UI test tags.

- **Two composables per screen**: a stateful entry point that takes the `ViewModel` and collects state, and a `@VisibleForTesting` stateless one that takes `state` + `onAction: (Action) -> Unit`. Hoist all state; the stateless version is what previews and UI tests drive. *(Copy-paste skeleton in [`../template.md`](../template.md).)*
- Collect state with **`collectAsStateWithLifecycle()`** (not `collectAsState()`).
- Collect effects once in a `LaunchedEffect`, branching to navigation/snackbar handlers.
- Drive spacing/sizing from **design-system tokens** (`SPACING_SMALL`, `SPACING_MEDIUM`, …) — no magic `dp` numbers scattered in features.
- Pull colors/typography from `MaterialTheme.colorScheme` / `MaterialTheme.typography`, themed by the design system.
- Resolve user-facing strings with **`stringResource`** / **`pluralStringResource`** — never build them by concatenation. (ViewModels resolve strings via an injected `ResourceProvider` instead — see the `architecture-layers` skill.)
- Provide **`@Preview`s** for every meaningful state (content, loading, empty, error). Use a custom multi-preview annotation (e.g. `@LightDarkPreviews`) to render light + dark at once.
- Keep composables **stateless and side-effect-free** except for the thin stateful wrapper.

> **Guardrail:** every new screen ships with the full set — a stateful + stateless composable pair, `@Preview`s for *every* state (content, loading, empty, error) and typed error handling that distinguishes offline from unknown failures.
