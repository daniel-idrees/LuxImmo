# Unit Testing — With Fakes

> Use when writing JVM unit tests with fakes, or building shared test infrastructure. **Fakes are the default doubling style in this architecture**; reach for Mockito only when you specifically need interaction verification.

**Test stack:** JUnit4 (runner + rules), `kotlinx-coroutines-test` (`runTest`, `TestDispatcher`), Turbine (`Flow`/state/effect assertions), and hand-written fakes. Mockito (with `mockito-kotlin`) is reached for deliberately — see the `unit-testing-with-mocks` skill.

## Shared test infrastructure (`:core:testing`)

- **Fakes over mocks** for collaborators with behavior: `Test<Thing>Repository` (lets tests push emissions and program refresh results/delays), `TestNetworkMonitor`, `TestResourceProvider` (returns predictable strings).
- **`MainDispatcherRule`** (a `TestWatcher` calling `Dispatchers.setMain/resetMain` with a `TestDispatcher`).
- **Canonical test data** factories (`<thing>TestData`) reused across modules.

## Why fakes

- A fake is a real, hand-written implementation of the interface with controllable behavior — it survives refactors better than a mock and exercises real `Flow` emissions.
- For stateful collaborators (repositories emitting `Flow`, a `NetworkMonitor` toggling connectivity), a fake reproduces real behavior; a mock would force you to re-script every emission.

## Test mechanics (ViewModels, use cases, mappers, repositories)

- Run on the JVM with `runTest`; install `MainDispatcherRule`; inject the rule's `testDispatcher` where a dispatcher is required.
- Assert **state** with **Turbine** (`viewModel.viewState.test { awaitItem() ... }`) and **effects** with `viewModel.effect.test { ... }`.
- Drive time with `advanceTimeBy` / `skipItems` to exercise debounce, delays, and sync transitions.
- Construct the ViewModel directly with fakes — no Hilt in unit tests.
- Cover the full matrix: initial state, success, empty, cached-but-refresh-fails (snackbar effect), empty-and-refresh-fails (full-screen error), retry-on-reconnect, and each user action.

A copy-paste test skeleton is in [`../template.md`](../template.md).

## Single-module variant

The fakes, `MainDispatcherRule`, and test-data factories live in the app module's `test` source set (or `testFixtures`) instead of a shared `:core:testing` module — reused across the module's tests rather than via `testImplementation(project(":core:testing"))`. Everything else (`runTest`, Turbine, the test matrix, the skeleton) is identical.
