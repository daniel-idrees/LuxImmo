@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.data.testdoubles

import com.example.network.GslNetworkDataSource
import com.example.network.demo.DemoGslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingsResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import java.io.IOException

class TestGslNetworkDataSource : GslNetworkDataSource {

    private val source = DemoGslNetworkDataSource(
        ioDispatcher = UnconfinedTestDispatcher()
    )

    enum class NetworkError {
        IO_EXCEPTION,
        HTTP_EXCEPTION,
        NONE
    }

    private var networkError = NetworkError.NONE

    fun setShouldThrowNetworkError(error: NetworkError) {
        networkError = error
    }

    override suspend fun getListings(): NetworkListingsResponse =
        when (networkError) {
            NetworkError.IO_EXCEPTION -> throw IOException("Test error: Network failure")
            NetworkError.HTTP_EXCEPTION -> throw IllegalStateException("Test error: Server failure")
            else -> source.getListings()
        }

    override suspend fun getListing(id: Int): NetworkListing? =
        when (networkError) {
            NetworkError.IO_EXCEPTION -> throw IOException("Test error: Network failure")
            NetworkError.HTTP_EXCEPTION -> throw IllegalStateException("Test error: Server failure")
            else -> source.getListing(id)
        }
}
