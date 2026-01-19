package com.example.listings.ui.detail

import app.cash.turbine.test
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.model.PropertyType
import com.example.domain.usecase.GetListingDetailUseCase
import com.example.listings.models.toListingUi
import com.example.listings.resource.TestResourceProvider
import com.example.listings.testdoubles.TestListingRepository
import com.example.listings.util.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DetailViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()
    private val listingRepository = TestListingRepository()
    private val resourceProvider = TestResourceProvider()
    private val useCase: GetListingDetailUseCase = GetListingDetailUseCase(listingRepository)
    private lateinit var viewModel: DetailViewModel

    @Before
    fun setup() {
        viewModel = DetailViewModel(useCase, resourceProvider, sampleListing.id)
    }

    private val sampleListing = Listing(
        id = 1,
        price = 1500000.0,
        area = 250.0,
        city = "Paris",
        bedrooms = 4,
        rooms = 8,
        imageUrl = "https://example.com/image.jpg",
        propertyType = PropertyType.MAISON_VILLA,
        vendor = "Luxe Properties",
        offerType = 1
    )

    private val sampleListingUi by lazy {
        sampleListing.toListingUi(resourceProvider)
    }

    @Test
    fun `state is initially loading`() = runTest {
        assertEquals(DetailUiState(isLoading = true), viewModel.viewState.value)
    }

    @Test
    fun `when repository returns data and refresh is success, state shows content`() = runTest {
        // The repository will successfully find the item on refresh
        listingRepository.setRefreshListingResult(Result.Success(sampleListing))
        // simulate DAO emitting the listing by sending it to the test repository's flow
        listingRepository.sendListings(listOf(sampleListing))

        viewModel.viewState.test {

            assertTrue(awaitItem().isLoading)

            // data load will start here
            viewModel.initialise()

            // Assert
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
            assertEquals(sampleListingUi, finalState.listing)
        }
    }

    @Test
    fun `when repository returns data but refresh is error, state still shows content`() = runTest {

        // refresh result will be error but repository will return data
        listingRepository.setRefreshListingResult(Result.Error(AppError.NoInternetConnection))
        listingRepository.sendListings(listOf(sampleListing))

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)

            // data load will start here
            viewModel.initialise()

            // Assert
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertNull(finalState.error)
            assertEquals(sampleListingUi, finalState.listing)
        }
    }

    @Test
    fun `when repository was empty initially but refresh is success with data, state will wait and show data from repository`() =
        runTest {
            // The repository will successfully find the item on refresh
            listingRepository.setRefreshListingResult(Result.Success(sampleListing))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // data load will start here
                viewModel.initialise()

                // Assert
                val finalState = awaitItem()
                assertFalse(finalState.isLoading)
                assertNull(finalState.error)
                assertEquals(sampleListingUi, finalState.listing)
            }
        }

    // When cached data is available but source confirms that is not available anymore
    @Test
    fun `when repository returns data and refresh is success with null data, state shows not found error`() =
        runTest {
            // refresh will succeed but find nothing
            listingRepository.setRefreshListingResult(Result.Success(null))
            // populate repository with cached data
            listingRepository.sendListings(listOf(sampleListing))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // start loading date
                viewModel.initialise()

                // Assert
                val finalState = awaitItem()
                assertFalse(finalState.isLoading)
                assertNotNull(finalState.error)
                assertEquals("Listing not found", finalState.error.errorText)
                assertNull(finalState.error.onRetry) // No retry for a 404
            }
        }

    // When cached data is available but source gives a different data
    @Test
    fun `when repository returns data and refresh is success with different data, state shows latest one`() =
        runTest {

            val sampleListingWithPriceIncrease = sampleListing.copy(
                price = 9500000.0,  //imagine price increased
            )

            // refresh will succeed with a different data
            listingRepository.setRefreshListingResult(Result.Success(sampleListingWithPriceIncrease))
            // populate repository with cached data
            listingRepository.sendListings(listOf(sampleListing))

            viewModel.viewState.test {
                assertTrue(awaitItem().isLoading)

                // start loading date
                viewModel.initialise()

                // Assert
                val finalState = awaitItem()
                assertFalse(finalState.isLoading)
                assertNull(finalState.error)
                val expectedListing = sampleListingWithPriceIncrease.toListingUi(resourceProvider)
                assertEquals(expectedListing, finalState.listing)
            }
        }

    @Test
    fun `when repository is empty and refresh fails, state shows retryable error`() = runTest {
        // Refresh will fail with a network error
        listingRepository.setRefreshListingResult(Result.Error(AppError.NoInternetConnection))
        // Making sure nothing is found in the repository
        listingRepository.sendListings(emptyList())

        viewModel.viewState.test {
            assertTrue(awaitItem().isLoading)

            // start loading the data
            viewModel.initialise()

            // Assert
            val finalState = awaitItem()
            assertFalse(finalState.isLoading)
            assertNotNull(finalState.error)
            assertEquals("Offline", finalState.error.errorText)
            assertNotNull(finalState.error.onRetry)
        }
    }
}
