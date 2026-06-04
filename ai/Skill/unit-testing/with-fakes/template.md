# Quick Logic Tests (with Fakes) — Template

Fill-in skeleton. Replace `<Screen>` / `<Thing>`. Construct the ViewModel directly with `:core:testing` fakes; assert state with Turbine.

```kotlin
@get:Rule val mainDispatcherRule = MainDispatcherRule()

@Test
fun emitsContent_whenRepositoryEmits() = runTest {
    val repository = Test<Thing>Repository()
    val viewModel = <Screen>ViewModel(
        resourceProvider = TestResourceProvider(),
        getItems = Get<Thing>UseCase(repository),
        networkMonitor = TestNetworkMonitor(),
        defaultDispatcher = mainDispatcherRule.testDispatcher,
    )

    viewModel.viewState.test {
        assertTrue(awaitItem().isLoading)        // initial state
        repository.emit(<thing>TestData)         // push an emission from the fake
        assertEquals(<thing>TestData.size, awaitItem().items.size)
    }
}
```
