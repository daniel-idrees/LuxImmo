package com.example.domain.usecase

import com.example.domain.repository.ListingRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GetListingDetailUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    operator fun invoke(listingId: Int) = listingsRepository
        .getListing(listingId)
        .onEach {
            if (it == null) {
                listingsRepository.refreshListingDetails(listingId)
            }
        }.distinctUntilChanged()
}
