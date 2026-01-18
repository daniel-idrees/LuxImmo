package com.example.domain.usecase

import com.example.domain.repository.ListingRepository
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    operator fun invoke() = listingsRepository.listings
    suspend fun refresh() = listingsRepository.refreshListings()
}
