# Architecture Layers — Templates

Fill-in skeletons. Replace `<Thing>` with your model name and adapt packages/types to your project.

## Domain `Result` and error model (`:core:domain`)

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
}

sealed interface AppError {
    data object NoInternetConnection : AppError
    data object Unknown : AppError
}
```

## Use case (`:core:domain`)

```kotlin
class Get<Thing>UseCase @Inject constructor(
    private val repository: <Thing>Repository
) {
    operator fun invoke(): Flow<List<<Thing>>> = repository.items
    suspend fun refresh(): Result<List<<Thing>>> = repository.refresh()
}
```

## Safe network-call wrapper (`:core:data`)

Never swallow coroutine cancellation; turn other failures into a typed `AppError` (e.g. `IOException → NoInternetConnection`, else `Unknown`).

```kotlin
inline fun <R> runSuspendCatching(block: () -> R): Result<R> = try {
    Result.success(block())
} catch (c: CancellationException) {
    throw c                      // never swallow coroutine cancellation
} catch (e: Throwable) {
    Result.failure(e)
}
```

## Offline-first repository (`:core:data`)

```kotlin
internal class OfflineFirst<Thing>Repository @Inject constructor(
    private val network: <Thing>NetworkDataSource,
    private val dao: <Thing>Dao,
) : <Thing>Repository {

    override val items: Flow<List<<Thing>>> =
        dao.getAll().map { entities -> entities.map(Entity::asExternalModel) }

    override suspend fun refresh(): Result<List<<Thing>>> =
        runSuspendCatching { network.get() }.fold(
            onSuccess = { dto ->
                val entities = dto.items.map(Dto::asEntity)
                dao.replaceAll(entities)
                Result.Success(entities.map(Entity::asExternalModel))
            },
            onFailure = { it.toErrorResult() }
        )
}
```

---

# MVI (the UI loop)

The contracts and the `MviViewModel` base live once in `:core:ui`; the per-screen State / Action / Effect and ViewModel are copied per feature. Replace `<Screen>` and `<Thing>`.

## MVI contracts (`:core:ui`, write once)

```kotlin
interface ViewState       // immutable; implemented by a data class
interface ViewAction      // user intent / event; implemented by a sealed interface
interface ViewSideEffect  // one-time event (navigate, snackbar); sealed interface
```

## MVI base ViewModel (`:core:ui`, write once)

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
