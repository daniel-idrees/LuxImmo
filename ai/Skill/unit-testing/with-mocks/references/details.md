# Unit Testing — With Mocks (Mockito)

> Use when a unit test needs to **verify interactions** (that a collaborator was called) or stub a one-off collaborator where a full fake is overkill. **Default to fakes**; reach for mocks deliberately.

## When to prefer a mock over a fake

- You need to assert *that* — and *how* — a collaborator was called (`verify`, argument captors), rather than the resulting state.
- The collaborator is stateless or used once, so a hand-written fake adds no value.
- Avoid mocks for stateful, `Flow`-emitting collaborators — a fake reproduces real emissions far more faithfully.

## Shared mechanics (same as with fakes)

- Run on the JVM with `runTest`; install `MainDispatcherRule`; inject the rule's `testDispatcher`.
- Assert **state** with **Turbine** and **effects** with `effect.test { ... }`.
- Construct the ViewModel directly — no Hilt in unit tests.
- Cover the same matrix: initial, success, empty, refresh-fails, retry-on-reconnect, and each action.

## Mockito (with mockito-kotlin)

A copy-paste test skeleton is in [`../template.md`](../template.md). Notes:

- Stub suspend functions with `onBlocking { ... } doReturn ...` (or `whenever(repo.refresh()).thenReturn(...)` inside `runTest`).
- Capture arguments with `argumentCaptor<T>()` when you need to assert on what was passed.
- Keep mocks shallow: stub only what the test exercises; don't re-mock an entire `Flow` pipeline a fake would model better. It's fine to mix — mock the collaborator under verification while using `:core:testing` fakes (`TestResourceProvider`, `TestNetworkMonitor`) for the rest.

## Single-module variant

Identical, except the fakes you mix in (`TestResourceProvider`, `TestNetworkMonitor`) live in the app module's `test` source set rather than a shared `:core:testing` module.
