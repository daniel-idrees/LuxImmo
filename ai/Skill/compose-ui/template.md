# Building Screens — Template

Fill-in skeleton. Replace `<Screen>`. Every screen is a thin stateful wrapper plus a stateless body that previews and UI tests drive.

## Stateful + stateless screen

```kotlin
@Composable
internal fun <Screen>Screen(viewModel: <Screen>ViewModel) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    <Screen>Screen(state = state, onAction = viewModel::setAction)
}

@VisibleForTesting
@Composable
internal fun <Screen>Screen(state: <Screen>UiState, onAction: (<Screen>UiAction) -> Unit) { /* ... */ }
```
