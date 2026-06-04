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
