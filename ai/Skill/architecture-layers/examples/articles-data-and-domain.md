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

DTO, a data-source interface, the Retrofit API declaration, and a Retrofit-backed implementation. Two return shapes are shown: `getArticles()` returns the object directly (any non-2xx becomes an `HttpException`), while `getArticle(id)` returns Retrofit's `Response` so a 403/404 can be mapped to `null` instead of throwing.

```kotlin
@Serializable
data class ArticleDto(
    val id: String,
    val title: String,
    val author: String,
    val body: String,
)

// The only network type the data layer sees — fakeable in tests
interface ArticleNetworkDataSource {
    suspend fun getArticles(): List<ArticleDto>     // direct object
    suspend fun getArticle(id: String): ArticleDto? // null when not found
}

// Retrofit API declaration — internal to :core:network
internal interface RetrofitArticleNetworkApi {
    // Direct object: a non-2xx response throws HttpException automatically
    @GET("articles.json")
    suspend fun getArticles(): List<ArticleDto>

    // Response<T>: lets the client read the HTTP status code
    @GET("articles/{id}.json")
    suspend fun getArticle(@Path("id") id: String): Response<ArticleDto?>
}

@Singleton
internal class RetrofitArticleApiClient @Inject constructor(
    private val networkApi: RetrofitArticleNetworkApi,
) : ArticleNetworkDataSource {

    override suspend fun getArticles(): List<ArticleDto> =
        networkApi.getArticles()

    // 403/404 → the article doesn't exist (null); any other non-2xx is rethrown
    override suspend fun getArticle(id: String): ArticleDto? {
        val response = networkApi.getArticle(id = id)
        return when {
            response.isSuccessful -> response.body()
            response.code() == 403 || response.code() == 404 -> null
            else -> throw HttpException(response)
        }
    }
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
