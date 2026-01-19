package com.example.luximmo.demo

import com.example.network.demo.DemoGslNetworkDataSource
import com.example.network.model.NetworkListing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoGslNetworkDataSourceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var subject: DemoGslNetworkDataSource

    @Before
    fun setUp() {
        subject = DemoGslNetworkDataSource(
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `listings json is correctly deserialized`() = runTest(testDispatcher) {
        val listingsResponse = subject.getListings()

        // Assert the total count is correct
        assertEquals(4, listingsResponse.totalCount)

        // Create the expected object for the first item in the JSON
        val expectedFirstListing = NetworkListing(
            id = 1,
            bedrooms = 4,
            city = "Villers-sur-Mer",
            area = 250.0,
            url = "https://v.seloger.com/s/crop/590x330/visuels/1/7/t/3/17t3fitclms3bzwv8qshbyzh9dw32e9l0p0udr80k.jpg",
            price = 1500000.0,
            professional = "GSL EXPLORE",
            propertyType = "Maison - Villa",
            offerType = 1,
            rooms = 8
        )

        // Assert that the first item in the deserialized list matches our expectation
        assertEquals(expectedFirstListing, listingsResponse.items.first())
    }
}