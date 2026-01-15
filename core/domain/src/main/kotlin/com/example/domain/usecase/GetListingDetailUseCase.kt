package com.example.domain.usecase

import com.example.domain.repository.ListingRepository
import javax.inject.Inject

class GetListingDetailUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    suspend operator fun invoke(listingId: Int) = listingsRepository.getListing(listingId)
}