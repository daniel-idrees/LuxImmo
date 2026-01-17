package com.example.domain.repository

import com.example.domain.Result
import com.example.domain.model.Listing
import kotlinx.coroutines.flow.Flow


interface ListingRepository {
    fun observeListings(): Flow<List<Listing>>
    fun getListing(listingId: Int): Flow<Listing>
    suspend fun refreshListings(): Result<List<Listing>>
}