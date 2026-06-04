# Example — an "Articles" data + domain slice

A generic, end-to-end illustration of the layers for a fictional **Articles** reader. It is not tied to any real project; copy the shape, not the names. Base types (`Result`, `AppError`, `runSuspendCatching`) come from this skill's `template.md`.

## Domain (`:core:domain`) — pure Kotlin

```kotlin
data class Article(
    val id: String,
    val title: String,
    val author: String,
    val body: String,
)

interface ArticleRepository {
    val articles: Flow<List<Article>>
    suspend fun refresh(): Result<List<Article>>
}

class GetArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository,
) {
    operator fun invoke(): Flow<List<Article>> = repository.articles
    suspend fun refresh(): Result<List<Article>> = repository.refresh()
}
```

## Network (`:core:network`)

```kotlin
@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val author: String,
    val body: String,
)

interface ArticleNetworkDataSource {
    suspend fun getArticles(): List<ArticleDto>
}
```

## Database (`:core:database`)

```kotlin
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val body: String,
)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles")
    fun getAll(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)
}
```

## Data (`:core:data`) — mappers + offline-first repository

```kotlin
// Mappers — one direction per function
fun ArticleDto.asEntity() = ArticleEntity(id, title, author, body)
fun ArticleEntity.asExternalModel() = Article(id, title, author, body)

internal class OfflineFirstArticleRepository @Inject constructor(
    private val network: ArticleNetworkDataSource,
    private val dao: ArticleDao,
) : ArticleRepository {

    // Reads come only from the database (single source of truth).
    override val articles: Flow<List<Article>> =
        dao.getAll().map { entities -> entities.map(ArticleEntity::asExternalModel) }

    // Refresh hits the network, writes to the DB; the DB Flow updates the UI.
    override suspend fun refresh(): Result<List<Article>> =
        runSuspendCatching { network.getArticles() }.fold(
            onSuccess = { dtos ->
                val entities = dtos.map(ArticleDto::asEntity)
                dao.upsertAll(entities)
                Result.Success(entities.map(ArticleEntity::asExternalModel))
            },
            onFailure = { it.toErrorResult() },
        )
}
```

## Common violations and their fixes

Before/after pairs for the two layer guardrails (dependency direction, one result type).

### A feature reaching into the data layer

```kotlin
// ❌ Violation — feature depends on :core:database and uses a DAO directly
class ArticlesViewModel(private val dao: ArticleDao) { /* … */ }

// ✅ Fix — depend on :core:domain and use a use case
class ArticlesViewModel(private val getArticles: GetArticlesUseCase) { /* … */ }
```

### A second result type

```kotlin
// ❌ Violation — a parallel Either/Outcome type alongside the domain Result
sealed interface Outcome<T> { /* … */ }

// ✅ Fix — reuse the domain Result + AppError everywhere
suspend fun refresh(): Result<List<Article>>
```
