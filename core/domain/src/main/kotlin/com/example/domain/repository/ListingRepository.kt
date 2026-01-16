package com.example.domain.repository

import com.example.domain.Result
import com.example.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow


interface ListingRepository {
    val listings: Flow<List<Listing>>
    fun getListing(listingId: Int): Flow<Listing?>

    suspend fun refreshListings(): Result
    suspend fun refreshListingDetails(listingId: Int): Result
    val refreshListResultEvent: SharedFlow<Result>
}