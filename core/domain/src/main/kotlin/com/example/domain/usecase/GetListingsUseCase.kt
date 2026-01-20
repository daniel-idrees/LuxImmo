package com.example.domain.usecase

import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A use case for retrieving a list of listings.
 */
class GetListingsUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    /**
     * Returns a list of listings with their associated followed state
     */
    operator fun invoke(): Flow<List<Listing>> = listingsRepository.listings

    /**
     * Refreshes listings from the source and returns the result of the operation
     */
    suspend fun refresh(): Result<List<Listing>> = listingsRepository.refreshListings()
}
