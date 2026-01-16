package com.example.domain.usecase

import com.example.domain.repository.ListingRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject


class GetListingsUseCase @Inject constructor(
    private val listingsRepository: ListingRepository
) {
    operator fun invoke() = listingsRepository.listings

    suspend fun refresh() = listingsRepository.refreshListings()

    val refreshResultEvent = listingsRepository.refreshListResultEvent
}