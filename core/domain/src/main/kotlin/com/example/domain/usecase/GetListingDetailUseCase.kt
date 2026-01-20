package com.example.domain.usecase

import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.repository.ListingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A use case for retrieving a specific listing by its identifier.
 */
class GetListingDetailUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    /**
     * Returns a listing with its associated followed state
     */
    operator fun invoke(listingId: Int): Flow<Listing?> = listingsRepository.getListing(listingId)

    /**
     * Refreshes a listing from the source and returns the result of the operation
     */
    suspend fun refresh(listingId: Int): Result<Listing?> = listingsRepository.refreshListing(listingId)
}
