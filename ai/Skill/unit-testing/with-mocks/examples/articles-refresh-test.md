# Example — verifying an interaction with a mock

A generic, filled-in unit test that uses Mockito (mockito-kotlin) because the goal is to **verify** that `refresh()` was called — not to assert on emitted state. Other collaborators stay as `:core:testing` fakes.

```kotlin
class ArticlesViewModelMockTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refresh_callsRepositoryRefresh() = runTest {
        val repository = mock<ArticleRepository> {
            on { articles } doReturn flowOf(emptyList())
            onBlocking { refresh() } doReturn Result.Success(emptyList())
        }

        val viewModel = ArticlesViewModel(
            resourceProvider = TestResourceProvider(),
            getArticles = GetArticlesUseCase(repository),
            networkMonitor = TestNetworkMonitor(),
            defaultDispatcher = mainDispatcherRule.testDispatcher,
        )

        viewModel.setAction(ArticlesUiAction.Refresh)

        verify(repository).refresh()                 // the point of this test
    }
}
```

Note the shallow mocking: only `articles` and `refresh()` are stubbed. For asserting on emitted *state* instead of interactions, prefer the fake-based approach.
