package com.example.network.demo

import com.example.common.di.IoDispatcher
import com.example.network.GslNetworkDataSource
import JvmUnitTestDemoAssetManager
import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingsResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import javax.inject.Inject

/**
 * This is a test-only implementation of the [GslNetworkDataSource] that uses local JSON files
 * as a data source. It is shared via `testFixtures`.
 */
class DemoGslNetworkDataSource @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val networkJson: Json = Json { ignoreUnknownKeys = true },
    private val assets: DemoAssetManager = JvmUnitTestDemoAssetManager
) : GslNetworkDataSource {

    override suspend fun getListings(): NetworkListingsResponse =
        withContext(ioDispatcher) {
            assets.open("listings.json").use { inputStream ->
                networkJson.decodeFromStream<NetworkListingsResponse>(inputStream)
            }
        }

    override suspend fun getListing(id: Int): NetworkListing? =
        getListings().items.firstOrNull { it.id == id }
}