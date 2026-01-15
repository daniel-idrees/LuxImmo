package com.example.domain.repository

import com.example.domain.Result
import com.example.domain.model.Listing
import kotlinx.coroutines.flow.Flow


interface ListingRepository {
    val listings: Flow<List<Listing>>
    suspend fun getListing(listingId: Int): Flow<Listing?>

    suspend fun refreshListings(): Result
    suspend fun refreshListingDetails(listingId: Int): com.example.domain.Result
}