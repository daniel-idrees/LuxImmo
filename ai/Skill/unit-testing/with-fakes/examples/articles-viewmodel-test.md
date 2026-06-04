# Example — testing the "Articles" ViewModel with a fake

A generic, filled-in unit test using a hand-written fake repository and Turbine. Runs on the JVM; no phone, no Hilt.

## The fake (lives in `:core:testing`)

```kotlin
class FakeArticleRepository : ArticleRepository {
    private val flow = MutableSharedFlow<List<Article>>(replay = 1)
    var refreshResult: Result<List<Article>> = Result.Success(emptyList())

    suspend fun emit(articles: List<Article>) = flow.emit(articles)

    override val articles: Flow<List<Article>> = flow
    override suspend fun refresh(): Result<List<Article>> = refreshResult
}
```

## The test

```kotlin
class ArticlesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeArticleRepository()

    private fun viewModel() = ArticlesViewModel(
        resourceProvider = TestResourceProvider(),
        getArticles = GetArticlesUseCase(repository),
        networkMonitor = TestNetworkMonitor(),
        defaultDispatcher = mainDispatcherRule.testDispatcher,
    )

    @Test
    fun showsArticles_whenRepositoryEmits() = runTest {
        val viewModel = viewModel()

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)                       // initial state
            repository.emit(listOf(Article("1", "Hello", "Ada", "…")))
            assertEquals(1, awaitItem().articles.size)
        }
    }

    @Test
    fun emitsSnackbar_whenRefreshFails() = runTest {
        repository.refreshResult = Result.Error(AppError.NoInternetConnection)
        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.setAction(ArticlesUiAction.Refresh)
            assertIs<ArticlesUiEffect.ShowSnackbar>(awaitItem())
        }
    }
}
```
