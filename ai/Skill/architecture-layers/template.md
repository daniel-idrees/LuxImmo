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

## Network (`:core:network`)

A `@Serializable` DTO, a data-source **interface** (so the network can be faked in tests), the Retrofit **API declaration**, and a Retrofit-backed **implementation** of the interface. Pick the return shape per endpoint:

- **Direct object** — return the DTO straight from Retrofit when any non-2xx should just propagate (Retrofit throws `HttpException` for you).
- **`Response<T>`** — return Retrofit's `Response` when the implementation needs the HTTP status (e.g. map 403/404 to `null` instead of throwing).

The Retrofit/OkHttp/JSON instance (JSON `ignoreUnknownKeys = true`, logging gated on `BuildConfig.DEBUG`, `BASE_URL` from `BuildConfig`) is provided via a Hilt module — see §4.3 in [`references/details.md`](references/details.md).

```kotlin
// DTO — mirrors the API shape; never leaks past the data layer
@Serializable
data class <Thing>Dto(
    val id: Int,
    // ... fields
)

// Data-source interface — the only network type the data layer depends on
interface <Thing>NetworkDataSource {
    suspend fun get<Thing>s(): <Thing>sResponse      // direct object
    suspend fun get<Thing>(id: Int): <Thing>Dto?     // null when not found
}

// Retrofit API declaration — internal to :core:network
internal interface Retrofit<Thing>NetworkApi {
    // Direct object: a non-2xx response throws HttpException automatically
    @GET("/<thing>s.json")
    suspend fun get<Thing>s(): <Thing>sResponse

    // Response<T>: lets the client inspect the HTTP status code
    @GET("<thing>s/{id}.json")
    suspend fun get<Thing>(@Path("id") id: Int): Response<<Thing>Dto?>
}

// Retrofit-backed implementation of the data-source interface
@Singleton
internal class Retrofit<Thing>ApiClient @Inject constructor(
    private val networkApi: Retrofit<Thing>NetworkApi,
) : <Thing>NetworkDataSource {

    // Direct object — returned as-is; transport/HTTP errors surface as HttpException
    override suspend fun get<Thing>s(): <Thing>sResponse =
        networkApi.get<Thing>s()

    // Response<T> — inspect the status: 403/404 mean "not found" → null,
    // any other non-2xx is a real error and is rethrown
    override suspend fun get<Thing>(id: Int): <Thing>Dto? {
        val response = networkApi.get<Thing>(id = id)
        return when {
            response.isSuccessful -> response.body()
            response.code() == 403 || response.code() == 404 -> null
            else -> throw HttpException(response)
        }
    }
}
```

### Hilt module (`:core:network`)

Provides the JSON, OkHttp call factory, Retrofit API, and binds the data-source interface to its Retrofit-backed implementation. `internal object` in `SingletonComponent`. Reads `BASE_URL` / `DEBUG` from `BuildConfig`; never hardcode them.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    @Provides
    @Singleton
    fun provideRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()

    @Provides
    @Singleton
    fun okHttpCallFactory(
        httpLoggingInterceptor: HttpLoggingInterceptor,
    ): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit<Thing>NetworkApi(
        retrofitBuilder: Retrofit.Builder,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
        networkJson: Json,
    ): Retrofit<Thing>NetworkApi =
        retrofitBuilder
            .baseUrl(BuildConfig.BASE_URL)
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(Retrofit<Thing>NetworkApi::class.java)

    // Bind the data-source interface to its Retrofit-backed implementation
    @Provides
    @Singleton
    fun provide<Thing>NetworkDataSource(
        network: Retrofit<Thing>ApiClient,
    ): <Thing>NetworkDataSource = network
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
