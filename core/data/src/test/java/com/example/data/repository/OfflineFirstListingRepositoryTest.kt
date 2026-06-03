@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.data.repository

import app.cash.turbine.test
import com.example.data.model.asEntity
import com.example.data.model.asExternalModel
import com.example.data.testdoubles.TestGslNetworkDataSource
import com.example.data.testdoubles.TestListingDao
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class OfflineFirstListingRepositoryTest {

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var repository: OfflineFirstListingRepository
    private lateinit var listingDao: TestListingDao
    private lateinit var networkDataSource: TestGslNetworkDataSource

    @Before
    fun setUp() {
        listingDao = TestListingDao()
        networkDataSource = TestGslNetworkDataSource()

        repository = OfflineFirstListingRepository(
            gslNetworkDataSource = networkDataSource,
            dao = listingDao
        )
    }

    @Test
    fun `listings flow should emit listings from DAO`() = testScope.runTest {
        //  Configure the test data source to not return an error
        networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.NONE)

        repository.listings.test {
            // The DAO should be empty initially
            assertEquals(emptyList<Listing>(), awaitItem())

            // Manually trigger a refresh, which populates the DAO
            repository.refreshListings()

            // The DAO should contain the expected listings
            val listingsFromDb = listingDao.getAllListings().first().map { it.asExternalModel() }
            val resultListings = awaitItem()

            // Assert
            assertEquals(listingsFromDb, resultListings)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshListings should return success with data when network is successful`() =
        testScope.runTest {
            //  Configure the test data source to not return an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.NONE)

            //  Manually trigger a refresh, which populates the DAO
            val result = repository.refreshListings()

            val expectedListings =
                networkDataSource.getListings().items.map { it.asEntity().asExternalModel() }

            // Assert
            assertTrue(result is Result.Success)
            assertEquals(expectedListings, (result as Result.Success).data)
        }

    @Test
    fun `refreshListings should return error when network fails with no internet connection`() =
        testScope.runTest {
            //  Configure the test data source to throw an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.IO_EXCEPTION)

            //  Call the refresh and assert it returns an error
            val result = repository.refreshListings()

            // Assert
            assertTrue(result is Result.Error && result.error == AppError.NoInternetConnection)
        }

    @Test
    fun `refreshListings delete the local listing when remote listing is deleted`() =
        testScope.runTest {

            //  Configure the test data source to not return an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.NONE)

            // populate the database by refresh
            repository.refreshListings()

            // prepare a fake listing and insert it in the database
            val fakeListing = listingDao.getAllListings().first().first().copy(id = 9999)
            listingDao.upsertListing(fakeListing)

            // fetch the listing details from network by refreshing the listing
            val result = repository.refreshListing(fakeListing.id)

            assertTrue(result is Result.Success)  //verify that there was no network error
            assertEquals(
                null,
                (result as Result.Success).data
            ) // verify that the result data was null

            val actual = listingDao.getListingById(fakeListing.id).first()

            // Assert
            assertEquals(null, actual)  //verify that listing is now deleted locally
        }


    @Test
    fun `getlisting flow should emit listing from DAO for the given listing id`() =
        testScope.runTest {
            //  Configure the test data source to not return an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.NONE)

            // repository will use this source to populate the listing, so we keep the id of the first item
            val fakeListingId = networkDataSource.getListings().items.first().id

            repository.getListing(fakeListingId).test {
                // local data is empty so we verify that we dont get any result
                assertEquals(null, awaitItem())

                // Manually trigger a refresh, which populates the DAO
                repository.refreshListings()

                // The DAO should contain the expected listings
                val listingFromDb =
                    listingDao.getListingById(fakeListingId).first()?.asExternalModel()
                val resultListing = awaitItem()

                // Assert
                assertEquals(listingFromDb, resultListing)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `refreshListing should return success with data when network is successful`() =
        testScope.runTest {
            // Configure the test data source to not return an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.NONE)

            // repository will use this source to populate the listing, so we keep one listing from the source
            val expectedListing =
                networkDataSource.getListings().items.first().asEntity().asExternalModel()

            // Manually trigger a refresh, which populates the DAO
            val result = repository.refreshListing(expectedListing.id)

            // Assert
            assertTrue(result is Result.Success)
            assertEquals(expectedListing, (result as Result.Success).data)
        }


    @Test
    fun `refreshListing should return no internet connection error when network fails`() =
        testScope.runTest {
            //  Configure the test data source to throw an error
            networkDataSource.setShouldThrowNetworkError(TestGslNetworkDataSource.NetworkError.IO_EXCEPTION)

            // call the refresh and assert it returns an error (id is irrelevant; the network throws first)
            val result = repository.refreshListing(listingId = 1)

            // Assert
            assertTrue(result is Result.Error && result.error == AppError.NoInternetConnection)
        }
}
