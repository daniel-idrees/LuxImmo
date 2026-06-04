# Example — the "Articles" screen MVI contract

A generic, filled-in version of the MVI skeleton for an **Articles** list screen. Base types (`ViewState`, `ViewAction`, `ViewSideEffect`, `MviViewModel`) come from this skill's `template.md`.

## State / Action / Effect

```kotlin
@Immutable
internal data class ArticlesUiState(
    val isLoading: Boolean = false,
    val articles: List<ArticleUi> = emptyList(),
    val errorConfig: UiErrorConfig? = null,
) : ViewState

internal sealed interface ArticlesUiAction : ViewAction {
    data object Init : ArticlesUiAction
    data object Refresh : ArticlesUiAction
    data class OnArticleClick(val article: ArticleUi) : ArticlesUiAction
}

internal sealed interface ArticlesUiEffect : ViewSideEffect {
    data class ShowSnackbar(val message: String) : ArticlesUiEffect
    data class NavigateToDetail(val id: String) : ArticlesUiEffect
}
```

## ViewModel

```kotlin
@HiltViewModel
internal class ArticlesViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getArticles: GetArticlesUseCase,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : MviViewModel<ArticlesUiAction, ArticlesUiState, ArticlesUiEffect>() {

    override fun setInitialState() = ArticlesUiState(isLoading = true)

    override fun handleAction(action: ArticlesUiAction) = when (action) {
        ArticlesUiAction.Init -> observeArticles()
        ArticlesUiAction.Refresh -> refresh()
        is ArticlesUiAction.OnArticleClick ->
            setEffect { ArticlesUiEffect.NavigateToDetail(action.article.id) }
    }

    private fun observeArticles() {
        getArticles()
            .map { articles -> articles.map { it.toArticleUi(resourceProvider) } }
            .flowOn(defaultDispatcher)                 // map off the main thread
            .onEach { ui -> setState { copy(isLoading = false, articles = ui) } }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        viewModelScope.launch {
            when (getArticles.refresh()) {
                is Result.Success -> Unit              // DB Flow updates the list
                is Result.Error -> setEffect {
                    ArticlesUiEffect.ShowSnackbar(
                        resourceProvider.getString(R.string.refresh_failed),
                    )
                }
            }
        }
    }
}
```

## Common violations and their fixes

Before/after pairs for the ViewModel guardrail (no `Context` / `Dispatchers` / string literals).

### Hard-coded dispatcher in a ViewModel

```kotlin
// ❌ Violation — Dispatchers.IO referenced directly; untestable without tricks
class ArticlesViewModel(...) {
    fun load() = viewModelScope.launch(Dispatchers.IO) { /* … */ }
}

// ✅ Fix — inject a qualified dispatcher
class ArticlesViewModel(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) {
    fun load() = viewModelScope.launch(defaultDispatcher) { /* … */ }
}
```

### User-facing string built in a ViewModel

```kotlin
// ❌ Violation — string literal / concatenation in business logic
setEffect { ArticlesUiEffect.ShowSnackbar("Failed to load articles") }

// ✅ Fix — go through ResourceProvider
setEffect { ArticlesUiEffect.ShowSnackbar(resourceProvider.getString(R.string.articles_load_failed)) }
```
