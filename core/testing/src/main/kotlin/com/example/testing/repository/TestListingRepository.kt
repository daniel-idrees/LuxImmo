package com.example.testing.repository

import com.example.domain.model.Listing
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import com.example.domain.Result
import com.example.domain.repository.ListingRepository

/**
 * A test-only implementation of the [com.example.domain.repository.ListingRepository] that can be controlled from tests.
 */
class TestListingRepository : ListingRepository {

    private val listingsFlow: MutableSharedFlow<List<Listing>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private var refreshListingResult: Result<Listing?>? = null
    private var refreshListingsResult: Result<List<Listing>>? = null

    private var refreshListingsDelayInMs : Long? = null

    override val listings: Flow<List<Listing>> = listingsFlow

    override fun getListing(listingId: Int): Flow<Listing?> =
        listingsFlow.map { listings -> listings.find { it.id == listingId } }

    override suspend fun refreshListings(): Result<List<Listing>> {
        refreshListingsDelayInMs?.let {
            delay(it)
        }

        val result = refreshListingsResult ?: Result.Success(
            listingsFlow.replayCache.firstOrNull() ?: emptyList()
        )

        if (result is Result.Success) {
            val refreshedListings = result.data
            sendListings(refreshedListings)
            return result
        }

        return result
    }

    override suspend fun refreshListing(listingId: Int): Result<Listing?> {
        val result = refreshListingResult ?: Result.Success(
            listingsFlow.replayCache.firstOrNull()?.find { it.id == listingId })

        if (result is Result.Success) {
            val currentListings = listingsFlow.replayCache.firstOrNull() ?: emptyList()
            val refreshedListing = result.data
            if (refreshedListing == null) {
                // If the refresh returned null, remove the item from our list.
                sendListings(currentListings.filterNot { it.id == listingId })
            } else {
                // If the refresh returned an item, upsert it into our list.
                val newList = currentListings.filterNot { it.id == listingId } + refreshedListing
                sendListings(newList)
            }
        }

        return result
    }

    /**
     * helper function to update local dao with new listings
     */
    suspend fun sendListings(listings: List<Listing>) {
        listingsFlow.emit(listings)
    }

    /**
     * helper function to set refresh listing behavior
     */
    fun setRefreshListingResult(result: Result<Listing?>) {
        refreshListingResult = result
    }

    /**
     * helper function to set refresh listings behavior
     */
    fun setRefreshListingsResult(result: Result<List<Listing>>) {
        refreshListingsResult = result
    }

    /**
     * helper function to set refresh listings delay
     */
    fun setRefreshListingsDelay(delayInMs: Long) {
        refreshListingsDelayInMs = delayInMs
    }
}