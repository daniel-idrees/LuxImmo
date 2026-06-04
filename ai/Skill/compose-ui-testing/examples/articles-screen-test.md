# Example — a UI test for the "Articles" screen

A generic, filled-in Compose UI test. It drives the **stateless** `ArticlesScreen` and asserts via the `testTag`s it declares.

```kotlin
class ArticlesScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsArticles_andReportsClicks() {
        var clicked: ArticleUi? = null

        composeRule.setContent {
            AppTheme {
                ArticlesScreen(
                    state = ArticlesUiState(
                        articles = listOf(
                            ArticleUi(id = "1", title = "Hello, Compose", byline = "by Ada"),
                            ArticleUi(id = "2", title = "State Hoisting", byline = "by Grace"),
                        ),
                    ),
                    onAction = { action ->
                        if (action is ArticlesUiAction.OnArticleClick) clicked = action.article
                    },
                )
            }
        }

        composeRule.onNodeWithText("Hello, Compose").assertIsDisplayed()
        composeRule.onNodeWithTag("article_1").performClick()

        assertEquals("1", clicked?.id)
    }
}
```
