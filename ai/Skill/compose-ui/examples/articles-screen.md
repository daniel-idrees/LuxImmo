# Example — the "Articles" screen

A generic, filled-in version of the stateful + stateless screen pair, with a preview. The stateless `ArticlesScreen` is what previews and UI tests drive.

```kotlin
@Composable
internal fun ArticlesScreen(viewModel: ArticlesViewModel) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current

    OneTimeLaunchedEffect { viewModel.setAction(ArticlesUiAction.Init) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ArticlesUiEffect.NavigateToDetail -> navigator.navigate(ArticleDetailKey(effect.id))
                is ArticlesUiEffect.ShowSnackbar -> { /* show snackbar */ }
            }
        }
    }

    ArticlesScreen(state = state, onAction = viewModel::setAction)
}

@VisibleForTesting
@Composable
internal fun ArticlesScreen(
    state: ArticlesUiState,
    onAction: (ArticlesUiAction) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(SPACING_MEDIUM)) {
        items(state.articles, key = { it.id }) { article ->
            ArticleRow(
                article = article,
                modifier = Modifier
                    .testTag("article_${article.id}")
                    .clickable { onAction(ArticlesUiAction.OnArticleClick(article)) },
            )
        }
    }
}

@LightDarkPreviews
@Composable
private fun ArticlesScreenPreview() {
    AppTheme {
        ArticlesScreen(
            state = ArticlesUiState(
                articles = listOf(
                    ArticleUi(id = "1", title = "Hello, Compose", byline = "by Ada"),
                    ArticleUi(id = "2", title = "State Hoisting", byline = "by Grace"),
                ),
            ),
            onAction = {},
        )
    }
}
```
