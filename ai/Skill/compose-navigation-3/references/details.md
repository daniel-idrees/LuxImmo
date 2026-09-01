
# Compose Navigation 3

> Use when adding destinations, nav keys, or feature entry providers.

- Destinations are type-safe **`NavKey`s** — `@Serializable` `data object`/`data class` implementing `NavKey`.
- Each feature exposes an **entry provider** extension (`EntryProviderScope<NavKey>.<feature>Entry(navigator)`) registering its destinations; `:app` assembles them into one back stack.
- A small **`Navigator`** wraps the `NavBackStack` with `navigate(key)` (reorder-to-top) and `goBack()`.
- Effects from ViewModels (`NavigateToDetail`) are translated to `navigator.navigate(...)` in the UI's effect collector — ViewModels never hold navigation objects.
- Support adaptive list/detail via Material 3 adaptive panes where relevant.

## Single-module variant

Navigation is identical. Each feature's entry-provider extension and its `NavKey`s live in the feature's package (`feature/<name>/`) instead of a feature module; the app's root nav host assembles them into one back stack just as before.
