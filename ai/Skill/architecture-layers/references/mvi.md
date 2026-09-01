# The UI Loop (MVI) in Detail

> Use when building a ViewModel and its State / Action / Effect contract.

> Copy-paste skeletons — the contracts, the `MviViewModel` base, and a screen's State/Action/Effect + ViewModel — are in [`../template.md`](../template.md); a filled-in example is in [`../examples/articles-list-mvi.md`](../examples/articles-list-mvi.md). The layer responsibilities these ViewModels sit on top of are in [`layers.md`](layers.md).

## The UI loop (MVI)

The UI layer is Jetpack Compose driven by a strict one-way loop: the user emits an **Action**, the ViewModel reduces it into immutable **State**, and the screen redraws from that state; one-time events go out as **Effects**. Because there is only one path for changes, screen behavior is easy to follow, reproduce, and fix. Use this when building a ViewModel and its State / Action / Effect contract. *(Copy-paste skeletons — the contracts, the `MviViewModel` base, and a screen's State/Action/Effect + ViewModel — are in [`../template.md`](../template.md); a filled-in example is in [`../examples/articles-list-mvi.md`](../examples/articles-list-mvi.md).)*

> MVI is the UI *pattern*; the composables that render the state are agnostic to it (they work equally with MVVM) and are outside this skill's scope.

## 5.1 Contracts (in `:core:ui`)

Three marker interfaces define the shape of every screen's contract: an immutable `ViewState` (a data class), a `ViewAction` (a sealed interface of user intents), and a `ViewSideEffect` (a sealed interface of one-time events).

## 5.2 Base ViewModel

A generic `MviViewModel` base manages the `StateFlow` of state, a buffered action `SharedFlow`, and a `Channel`-backed effect `Flow`. Key behaviors:

- State updates **only** through `setState { copy(...) }` — never mutate, always reduce.
- One-time events go through `setEffect` (delivered via `Channel`/`receiveAsFlow`) so they survive config changes and are not replayed.
- Actions flow in through `setAction` and are dispatched in `handleAction`.

## 5.3 A feature's MVI contract

Each screen owns four files (or one cohesive set): its `<Screen>UiState`, `<Screen>UiAction`, `<Screen>UiEffect`, and `<Screen>ViewModel`.

**Conventions captured in the templates:**

- ViewModels and their state/action/effect types are `internal` to the feature module.
- State is an **immutable** `data class` — `val`s with sensible defaults, annotated `@Immutable`. Model `Action`/`Effect` (and the domain `Result`, §4.1 in [`layers.md`](layers.md)) as `sealed interface`/`sealed class` so every `when` is exhaustive with **no `else`** branch.
- ViewModels never touch Android `Context`, string resources directly, or `Dispatchers.X` literals — they take a `ResourceProvider` and an injected dispatcher.
- Heavy transformation (mapping/sorting) runs on the injected `@DefaultDispatcher` via `flowOn`.
- Use `combine`, `distinctUntilChanged`, `shareIn(WhileSubscribed(5000), replay = 1)` to merge sources and avoid duplicate upstream work.
- An explicit `Init` action triggers first load (paired with `OneTimeLaunchedEffect` in the UI).

> **Guardrail:** a ViewModel never touches Android `Context`, `Dispatchers.X` literals, or user-facing string literals — inject `ResourceProvider` and a qualified dispatcher instead. Reuse the shared `MviViewModel` base rather than hand-rolling a second state/effect mechanism. (Before/after fixes for both are in [`../examples/articles-list-mvi.md`](../examples/articles-list-mvi.md).)

## MVI in a single module

The pattern is identical. The contracts and the `MviViewModel` base live in a `ui/` package instead of `:core:ui`; a screen's `State` / `Action` / `Effect` + ViewModel live in that feature's `feature/<name>/` package. With one module, `internal` no longer scopes a type to a single feature, so keep each screen's MVI types together in its package and rely on that boundary.
