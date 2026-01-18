package com.example.domain.repository

import com.example.domain.Result
import com.example.domain.model.Listing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing and accessing listing data.
 *
 * This repository acts as the central data provider for listings, abstracting
 * the underlying data sources from the rest of the application.
 */
interface ListingRepository {
    /**
     * Provides a continuous stream of listing data.
     *
     * @return A [Flow] emitting a list of [Listing] objects.
     */
    val listings: Flow<List<Listing>>

    /**
     * Retrieves a stream for a specific listing by its identifier.
     *
     * @param listingId The unique identifier of the listing.
     * @return A [Flow] emitting the requested [Listing].
     */
    fun getListing(listingId: Int): Flow<Listing?>

    /**
     * Performs a manual refresh of the listing data from the primary source.
     *
     * @return A [Result] indicating success with the refreshed data or a failure state.
     */
    suspend fun refreshListings(): Result<List<Listing>>

    /**
     * Performs a manual refresh of the specific listing data from the primary source.
     *
     * @param listingId The unique identifier of the listing.
     * @return A [Result] indicating success with the refreshed data or a failure state.
     */
    suspend fun refreshListing(listingId: Int): Result<Listing>
}