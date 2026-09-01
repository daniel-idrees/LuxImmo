# Quick Logic Tests (with Mocks) — Template

Fill-in skeleton (mockito-kotlin). Replace `<Screen>` / `<Thing>`. Mock the collaborator under verification; use `:core:testing` fakes for the rest.

```kotlin
@get:Rule val mainDispatcherRule = MainDispatcherRule()

@Test
fun refreshes_andVerifiesInteraction() = runTest {
    val repository = mock<<Thing>Repository> {
        on { items } doReturn flowOf(<thing>TestData)
        onBlocking { refresh() } doReturn Result.Success(<thing>TestData)
    }
    val viewModel = <Screen>ViewModel(
        resourceProvider = TestResourceProvider(),
        getItems = Get<Thing>UseCase(repository),
        networkMonitor = TestNetworkMonitor(),
        defaultDispatcher = mainDispatcherRule.testDispatcher,
    )

    viewModel.setAction(<Screen>UiAction.Refresh)

    verify(repository).refresh()                 // interaction verification
}
```
