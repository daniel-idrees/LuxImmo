package com.example.listings.ui.list

import app.cash.turbine.test
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.toListingUi
import com.example.listings.resource.TestResourceProvider
import com.example.testing.data.listingsTestData
import com.example.testing.repository.TestListingRepository
import com.example.testing.util.MainDispatcherRule
import com.example.testing.util.TestNetworkMonitor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val listingRepository = TestListingRepository()
    private val resourceProvider = TestResourceProvider()
    private val networkMonitor = TestNetworkMonitor()
    private val useCase = GetListingsUseCase(listingRepository)

    private lateinit var viewModel: ListingViewModel

    private val testListingUi by lazy {
        listingsTestData.map { it.toListingUi(resourceProvider) }
    }

    @Before
    fun setup() {
        viewModel = ListingViewModel(
            resourceProvider = resourceProvider,
            getListingsUseCase = useCase,
            networkMonitor = networkMonitor,
            defaultDispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `state is initially loading`() = runTest {
        assertEquals(ListingUiState(isLoading = true), viewModel.viewState.value)
    }

    @Test
    fun `state has initially default sorting option set`() = runTest {
        assertEquals(ListingSortOption.Default, viewModel.viewState.value.activeSort)
    }

    @Test
    fun `when repository contains data and refresh succeed, state contains listings`() = runTest {
        // Send listings from the repository
        listingRepository.sendListings(listingsTestData)
        // Make the repository return success
        listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)

            // Init
            viewModel.setAction(ListingUiAction.Init)

            // Assert
            val listingsState = awaitItem()
            assertFalse(listingsState.isLoading)
            assertEquals(null, listingsState.errorConfig)
            val expectedListing = testListingUi
            assertEquals(expectedListing, listingsState.listings)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when repository contains data and refresh succeed with a different data, state contains updated listings`() =
        runTest {
            // Send listings from the repository
            listingRepository.sendListings(listingsTestData)

            val updatedListing = listingsTestData.map {
                it.copy(
                    price = it.price + 500000 //imagine aur listings have updated price
                )
            }

            // Make the repository return success
            listingRepository.setRefreshListingsResult(Result.Success(updatedListing))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // Assert
                val listingsState = awaitItem()
                assertFalse(listingsState.isLoading)
                assertEquals(null, listingsState.errorConfig)

                val expectedListing = updatedListing.map { it.toListingUi(resourceProvider) }
                assertEquals(expectedListing, listingsState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when repository send empty list but refresh succeed with data, state contains updated listings`() =
        runTest {
            // Clear the repository
            listingRepository.sendListings(emptyList())
            // Make the repository fail
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

            viewModel.viewState.test {
                // Initial loading state
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                val listingsState = awaitItem()

                // Assert
                assertFalse(listingsState.isLoading)
                assertEquals(null, listingsState.errorConfig)
                val expectedListing = testListingUi
                assertEquals(expectedListing, listingsState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when repository send empty list, refresh is delayed, loader and refreshing loader should be visible`() =
        runTest {
            // Clear the repository
            listingRepository.sendListings(emptyList())
            // Make the repository fail
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))
            // Make the repository delay the sync
            listingRepository.setRefreshListingsDelay(5000)

            viewModel.viewState.test {
                // Initial loading state
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                val initialState = awaitItem()

                // Assert
                assertTrue(initialState.isLoading)
                assertTrue(initialState.isRefreshing)
                assertEquals(null, initialState.errorConfig)
                assertEquals(emptyList(), initialState.listings)

                advanceTimeBy(5001)

                val afterSyncCompleteState = awaitItem()

                assertFalse(afterSyncCompleteState.isLoading)
                assertFalse(afterSyncCompleteState.isRefreshing)

                val finalState = awaitItem()

                val expectedListing = testListingUi
                assertEquals(expectedListing, finalState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // repository is empty and we confirm from server which fails. We are not sure if
    // results are actually empty so we will show full error screen
    @Test
    fun `when repository send empty list and refresh fails with no internet error, state shows full-screen error`() =
        runTest {
            // Clear the repository
            listingRepository.sendListings(emptyList())
            // Make the repository fail
            listingRepository.setRefreshListingsResult(Result.Error(AppError.NoInternetConnection))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // The state should now show an error
                val errorState = awaitItem()

                // Assert
                assertFalse(errorState.isLoading)
                assertNotNull(errorState.errorConfig)
                assertNotNull(errorState.errorConfig::onRetry)
                assertEquals("Offline", errorState.errorConfig.errorText)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // repository is empty and we confirm from server which fails. We are not sure if
    // results are actually empty so we will show full error screen
    @Test
    fun `when repository send empty list and refresh fails with unknown error, state shows full-screen error`() =
        runTest {
            // Clear the repository
            listingRepository.sendListings(emptyList())
            // Make the repository fail
            listingRepository.setRefreshListingsResult(Result.Error(AppError.Unknown))

            viewModel.viewState.test {
                // Initial loading state
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // The state should now show an error
                val errorState = awaitItem()

                // Assert
                assertFalse(errorState.isLoading)
                assertNotNull(errorState.errorConfig)
                assertNotNull(errorState.errorConfig::onRetry)
                assertEquals("An unknown error occurred", errorState.errorConfig.errorText)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // Data is available offline but refresh fails, we should notify the user
    // data is not up to date
    @Test
    fun `when repository returns data but refresh fails with no internet error, effect shows snackbar`() =
        runTest {
            // Arrange: First, successfully load data into the cache
            listingRepository.sendListings(listingsTestData)
            // Now, make the next refresh fail
            listingRepository.setRefreshListingsResult(Result.Error(AppError.NoInternetConnection))

            viewModel.setAction(ListingUiAction.Init)

            viewModel.effect.test {


                // Assert: A snackbar effect was sent
                assertEquals("Offline", (awaitItem() as ListingUiEffect.ShowSnackbar).message)

                cancelAndIgnoreRemainingEvents()
            }

            // Also assert that the UI still shows the cached data and is not loading
            assertFalse(viewModel.viewState.value.isLoading)
            assertFalse(viewModel.viewState.value.isRefreshing)
            assertNull(viewModel.viewState.value.errorConfig)
            val expectedList = testListingUi
            assertEquals(expectedList, viewModel.viewState.value.listings)
        }

    // Data is available offline but refresh fails, we should notify the user
    // data is not up to date
    @Test
    fun `when repository returns data but refresh fails with no unknown error, effect shows snackbar`() =
        runTest {
            // Arrange: First, successfully load data into the cache
            listingRepository.sendListings(listingsTestData)
            // Now, make the next refresh fail
            listingRepository.setRefreshListingsResult(Result.Error(AppError.Unknown))

            // Init
            viewModel.setAction(ListingUiAction.Init)

            viewModel.effect.test {

                // Assert: A snackbar effect was sent
                assertEquals(
                    "An unknown error occurred",
                    (awaitItem() as ListingUiEffect.ShowSnackbar).message
                )
                cancelAndIgnoreRemainingEvents()
            }

            // Also assert that the UI still shows the cached data and is not loading
            assertFalse(viewModel.viewState.value.isLoading)
            assertFalse(viewModel.viewState.value.isRefreshing)
            assertNull(viewModel.viewState.value.errorConfig)
            val expectedList = testListingUi
            assertEquals(expectedList, viewModel.viewState.value.listings)
        }

    // Data is empty locally but refresh also confirms that list will remain empty
    @Test
    fun `when repository is empty and refresh succeed but with no data, state does not contains listings or loader`() = runTest {
        // Send listings from the repository
        listingRepository.sendListings(emptyList())
        // Make the repository return success
        listingRepository.setRefreshListingsResult(Result.Success(emptyList()))

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)

            // Init
            viewModel.setAction(ListingUiAction.Init)

            // Assert
            val listingsState = awaitItem()
            assertFalse(listingsState.isLoading)
            assertFalse(listingsState.isRefreshing)
            assertEquals(null, listingsState.errorConfig)
            assertEquals(emptyList(), listingsState.listings)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // Refresh received listings initially but after a manual refresh, it receives different listing
    @Test
    fun `when repository is empty and refresh succeed with no data, manual refresh succeeds with a new data, state contains updated listings`() =
        runTest {
            // Send listings from the repository
            listingRepository.sendListings(emptyList())
            // Make the repository return success with same list
            listingRepository.setRefreshListingsResult(Result.Success(emptyList()))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // Assert
                val listingsState = awaitItem()
                assertFalse(listingsState.isLoading)
                assertFalse(listingsState.isRefreshing)
                assertEquals(null, listingsState.errorConfig)


                assertEquals(emptyList(), listingsState.listings)

                //refresh should now get a different result that will update repo
                listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

                // Introduce a delay to simulate a real network call
                listingRepository.setRefreshListingsDelay(100) // 100ms is enough
                // Manual refresh
                viewModel.setAction(ListingUiAction.Refresh)
                assertTrue(awaitItem().isRefreshing)

                skipItems(1) //Sync status will update the state with finished value

                // Assert Final State
                val finalState = awaitItem()
                assertFalse(finalState.isRefreshing)
                assertFalse(finalState.isLoading)
                val expectedListing = testListingUi
                assertEquals(expectedListing, finalState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // Refresh received listings initially but after a manual refresh, it receives different listing
    @Test
    fun `when manual refresh succeeds with a different data, state contains updated listings`() =
        runTest {
            // Send listings from the repository
            listingRepository.sendListings(listingsTestData)
            // Make the repository return success with same list
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

            val updatedListing = listingsTestData.map {
                it.copy(
                    price = it.price + 500000 //imagine aur listings have updated price
                )
            }

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // Assert
                val listingsState = awaitItem()
                assertFalse(listingsState.isLoading)
                assertFalse(listingsState.isRefreshing)
                assertEquals(null, listingsState.errorConfig)


                val expectedListing = testListingUi
                assertEquals(expectedListing, listingsState.listings)

                //refresh should now get a different result that will update repo
                listingRepository.setRefreshListingsResult(Result.Success(updatedListing))


                // Introduce a delay to simulate a real network call
                listingRepository.setRefreshListingsDelay(100) // 100ms is enough
                // Manual refresh
                viewModel.setAction(ListingUiAction.Refresh)
                assertTrue(awaitItem().isRefreshing)

                skipItems(1) //Sync status will update the state with finished value

                // Assert Final State
                val finalState = awaitItem() //final state will have listings
                assertFalse(finalState.isRefreshing)
                assertFalse(finalState.isLoading)

                val expectedUpdatedListing = updatedListing.map { it.toListingUi(resourceProvider) }
               assertEquals(expectedUpdatedListing, finalState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // Refresh received listings initially but after a manual refresh, it receives same listing
    @Test
    fun `when manual refresh succeeds with the same data, state contains same initial listings`() =
        runTest {
            // Send listings from the repository
            listingRepository.sendListings(listingsTestData)
            // Make the repository return success with same list
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))


            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                // Assert
                val listingsState = awaitItem()
                assertFalse(listingsState.isLoading)
                assertFalse(listingsState.isRefreshing)
                assertEquals(null, listingsState.errorConfig)

                val expectedListing = testListingUi
                assertEquals(expectedListing, listingsState.listings)


                // Introduce a delay to simulate a real network call
                listingRepository.setRefreshListingsDelay(100) // 100ms is enough
                // Manual refresh
                viewModel.setAction(ListingUiAction.Refresh)
                assertTrue(awaitItem().isRefreshing)

                // Assert Final State
                val finalState = awaitItem()
                assertFalse(finalState.isRefreshing)
                assertFalse(finalState.isLoading)

                assertEquals(expectedListing, finalState.listings)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `during manual refresh, only refresh loader should be visible no and no main loader if listings are available in state`() =
        runTest {
            // Keep the data ready
            listingRepository.sendListings(listingsTestData)
            // Make the repository fail
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

            viewModel.viewState.test {
                // Initial loading state
                assertTrue(awaitItem().isLoading)

                // Init
                viewModel.setAction(ListingUiAction.Init)

                val initialState = awaitItem()

                val expectedListings = testListingUi
                assertEquals(expectedListings, initialState.listings)

                // Make the repository delay the sync
                listingRepository.setRefreshListingsDelay(5000)

                // Refresh
                viewModel.setAction(ListingUiAction.Refresh)

                val finalState = awaitItem()

                // Assert
                assertFalse(finalState.isLoading)
                assertTrue(finalState.isRefreshing)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `when state contains listing, OnListingClick action should update state with selected listing and navigate to detail`() =
        runTest {
            // Send listings from the repository
            listingRepository.sendListings(listingsTestData)
            // Make the repository return success
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

            // Init
            viewModel.setAction(ListingUiAction.Init)
            val listingToSelect = listingsTestData.first().toListingUi(resourceProvider)

            // Act: Select a listing
            viewModel.setAction(ListingUiAction.OnListingClick(listingToSelect))

            viewModel.effect.test {

                // Assert: A snackbar effect was sent
                assertEquals(
                    ListingUiEffect.NavigateToDetail(listingToSelect.id),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(listingToSelect, viewModel.viewState.value.selectedListing)
        }

    @Test
    fun `when state contains listing and a selected listing, RemoveSelection action should remove the selected listing in the state`() =
        runTest {
            // Keep the data ready
            listingRepository.sendListings(listingsTestData)
            // Make the repository return success
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))

            // Init
            viewModel.setAction(ListingUiAction.Init)

            val listingToSelect = listingsTestData.first().toListingUi(resourceProvider)
            // select a listing
            viewModel.setAction(ListingUiAction.OnListingClick(listingToSelect))

            // Assert that a listing is selected
            assertEquals(listingToSelect, viewModel.viewState.value.selectedListing)

            // Act: remove the selected listing
            viewModel.setAction(ListingUiAction.RemoveSelection)
            assertNull(viewModel.viewState.value.selectedListing)
        }

    @Test
    fun `when network returns from offline, retry is triggered`() = runTest {
        // Get the app into a full-screen error state
        // Make the repository and refresh fail
        listingRepository.sendListings(emptyList())
        listingRepository.setRefreshListingsResult(Result.Error(AppError.NoInternetConnection))

        networkMonitor.setIsOnline(false)

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)

            // Init
            viewModel.setAction(ListingUiAction.Init)

            // Confirm we are in the error state
            assertNotNull((awaitItem().errorConfig))

            // Ensure the next refresh will succeed
            listingRepository.setRefreshListingsResult(Result.Success(listingsTestData))
            // Simulate network coming back online
            networkMonitor.setIsOnline(true)

            advanceTimeBy(1001)
            assertTrue(awaitItem().isRefreshing)

            val stateAfterSuccessful = awaitItem()

            assertFalse(stateAfterSuccessful.isRefreshing) //refresh loader is off because sync is finished

            val finalState = awaitItem()
            assertFalse(finalState.isLoading) //loader is not present
            assertEquals(null, finalState.errorConfig) // error is gone

            val expectedListing = testListingUi
            assertEquals(expectedListing, finalState.listings)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when sort option changes to price asc, list is sorted`() = runTest {
        // Keep the data ready
        listingRepository.sendListings(listingsTestData)

        // Init
        viewModel.setAction(ListingUiAction.Init)

        viewModel.viewState.test {
            // Initial default-sorted list
            val initialList = awaitItem().listings
            assertEquals(listingsTestData[0].id, initialList[0].id)

            // Change sort option to Price Ascending
            viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.PriceAsc))

            // Sort option and sorted list now update together in a single state emission
            val sortedState = awaitItem()
            assertEquals(ListingSortOption.PriceAsc, sortedState.activeSort)
            assertEquals(
                listingsTestData[1].id, // listing on index has lower price and should be first
                sortedState.listings[0].id
            )
        }
    }

    @Test
    fun `when sort option changes to price desc, list is sorted`() = runTest {
        // Keep the data ready
        listingRepository.sendListings(listingsTestData)

        // Init
        viewModel.setAction(ListingUiAction.Init)

        viewModel.viewState.test {
            // Initial default-sorted list
            val initialList = awaitItem().listings
            assertEquals(listingsTestData[0].id, initialList[0].id)

            // Change sort option to Price Descending
            viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.PriceDesc))

            // Sort option and sorted list now update together in a single state emission
            val sortedState = awaitItem()
            assertEquals(ListingSortOption.PriceDesc, sortedState.activeSort)
            assertEquals(
                listingsTestData[3].id, // Listing on index 3 has higher price and should be first
                sortedState.listings[0].id
            )
        }
    }

    @Test
    fun `when sort option changes to price per m2 desc, and further price desc to tie break, list is sorted`() = runTest {
        listingRepository.sendListings(listingsTestData)
        viewModel.setAction(ListingUiAction.Init)

        viewModel.viewState.test {
            awaitItem() // Skip initial state

            // Action: Price per Square Meter Descending (Highest first)
            viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.PricePerSquareMeterDesc))

            // Sort option and sorted list now update together in a single state emission
            val sortedState = awaitItem()
            assertEquals(ListingSortOption.PricePerSquareMeterDesc, sortedState.activeSort)

            val sortedList = sortedState.listings
            // ID 1 or 2 have 2,000 per m2 (Highest), but ID 1 is expensive than 2 so ID 1 will be first
            assertEquals(1, sortedList[0].id)
            // ID 3 has ~666 per m2 (Lowest)
            assertEquals(3, sortedList.last().id)
        }
    }

    @Test
    fun `when sort option changes to price per m2 asc, and further price asc to tie break, list is sorted`() = runTest {
        listingRepository.sendListings(listingsTestData)
        viewModel.setAction(ListingUiAction.Init)

        viewModel.viewState.test {
            awaitItem() // Skip initial state

            // Action: Price per Square Meter Ascending (Lowest first)
            viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.PricePerSquareMeterAsc))

            // Sort option and sorted list now update together in a single state emission
            val sortedState = awaitItem()
            assertEquals(ListingSortOption.PricePerSquareMeterAsc, sortedState.activeSort)

            val sortedList = sortedState.listings
            // ID 3 has 666 per m2 (Lowest)
            assertEquals(3, sortedList[0].id)
            // ID 1 or 2 have 2,000 per m2 (Highest) but ID 2 is cheaper than ID 1, then ID 1 should be last
            assertEquals(1, sortedList.last().id)
        }
    }

    @Test
    fun `when sort option changes to other option and then to default, actual order of the list is restored`() =
        runTest {
            // Keep the data ready
            listingRepository.sendListings(listingsTestData)

            // Init
            viewModel.setAction(ListingUiAction.Init)

            viewModel.viewState.test {
                // Initial default-sorted list
                val initialList = awaitItem().listings
                assertEquals(listingsTestData[0].id, initialList[0].id) // Listing with ID 1 is first

                // Change sort option to Price Ascending
                viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.PriceAsc))

                // Sort option and sorted list now update together in a single state emission
                val sortedState = awaitItem()
                assertEquals(ListingSortOption.PriceAsc, sortedState.activeSort)
                assertEquals(
                    listingsTestData[1].id, // Listing on index 1 has lowest area and should be first
                    sortedState.listings[0].id
                ) // Listing with ID 2 has lower area and should be first

                // Change sort option back to Default
                viewModel.setAction(ListingUiAction.OnSortChange(ListingSortOption.Default))

                // Restored order and sort option update together in a single state emission
                val defaultState = awaitItem()
                assertEquals(ListingSortOption.Default, defaultState.activeSort)
                assertEquals(
                    listingsTestData[0].id,
                    defaultState.listings[0].id
                )
            }
        }
}
