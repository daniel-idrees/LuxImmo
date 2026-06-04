# Example — compose navigation 3 for the "Articles" feature

A generic, filled-in version of type-safe destinations, a feature entry provider, and how a ViewModel effect becomes a navigation. Base types (`NavKey`, `Navigator`, `EntryProviderScope`) belong to `:core:ui` / the app.

## Destinations (type-safe keys)

```kotlin
@Serializable data object ArticlesListKey : NavKey
@Serializable data class ArticleDetailKey(val id: String) : NavKey
```

## The feature's entry provider

```kotlin
fun EntryProviderScope<NavKey>.articlesEntry(navigator: Navigator) {
    entry<ArticlesListKey> {
        ArticlesScreen(viewModel = hiltViewModel())
    }
    entry<ArticleDetailKey> { key ->
        ArticleDetailScreen(articleId = key.id)
    }
}
```

## Registered once in `:app`

```kotlin
NavDisplay(backStack = navigator.backStack) {
    articlesEntry(navigator)
    // otherFeatureEntry(navigator)
}
```

## A ViewModel effect becomes a navigation (in the UI's effect collector)

```kotlin
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is ArticlesUiEffect.NavigateToDetail -> navigator.navigate(ArticleDetailKey(effect.id))
            is ArticlesUiEffect.ShowSnackbar -> { /* … */ }
        }
    }
}
```

The ViewModel only emits `NavigateToDetail(id)`; it never holds a navigation object.
