# MVI Pattern — Templates

Fill-in skeletons. The contracts and the `MviViewModel` base live once in `:core:ui`; the per-screen State / Action / Effect and ViewModel are copied per feature. Replace `<Screen>` and `<Thing>`.

## Contracts (`:core:ui`, write once)

```kotlin
interface ViewState       // immutable; implemented by a data class
interface ViewAction      // user intent / event; implemented by a sealed interface
interface ViewSideEffect  // one-time event (navigate, snackbar); sealed interface
```

## Base ViewModel (`:core:ui`, write once)

Manages the `StateFlow` of state, a buffered action `SharedFlow`, and a `Channel`-backed effect `Flow`.

```kotlin
abstract class MviViewModel<Action : ViewAction, UiState : ViewState, Effect : ViewSideEffect>
    : ViewModel() {

    abstract fun setInitialState(): UiState
    val viewState: StateFlow<UiState>            // exposed to the UI
    val effect: Flow<Effect>                     // collected once by the UI

    fun setAction(action: Action)                // UI → ViewModel
    protected abstract fun handleAction(action: Action)
    protected fun setState(reducer: UiState.() -> UiState)
    protected fun setEffect(builder: () -> Effect)
}
```

## A screen's State / Action / Effect (per feature)

```kotlin
// State — @Immutable data class with sensible defaults
@Immutable
internal data class <Screen>UiState(
    val isLoading: Boolean = false,
    val items: List<<Thing>Ui> = emptyList(),
    val errorConfig: UiErrorConfig? = null,
) : ViewState

// Action — sealed interface of intents
internal sealed interface <Screen>UiAction : ViewAction {
    data object Init : <Screen>UiAction
    data object Refresh : <Screen>UiAction
    data class OnItemClick(val item: <Thing>Ui) : <Screen>UiAction
}

// Effect — sealed interface of one-time events
internal sealed interface <Screen>UiEffect : ViewSideEffect {
    data class ShowSnackbar(val message: String) : <Screen>UiEffect
    data class NavigateToDetail(val id: Int) : <Screen>UiEffect
}
```

## A screen's ViewModel (per feature)

```kotlin
@HiltViewModel
internal class <Screen>ViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getItems: Get<Thing>UseCase,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : MviViewModel<<Screen>UiAction, <Screen>UiState, <Screen>UiEffect>() {

    override fun setInitialState() = <Screen>UiState(isLoading = true)

    override fun handleAction(action: <Screen>UiAction) = when (action) {
        <Screen>UiAction.Init -> observeData()
        <Screen>UiAction.Refresh -> refresh()
        is <Screen>UiAction.OnItemClick -> {
            setState { copy(selected = action.item) }
            setEffect { <Screen>UiEffect.NavigateToDetail(action.item.id) }
        }
    }
    // ... combine flows, map to UI models on defaultDispatcher, reduce into state
}
```
