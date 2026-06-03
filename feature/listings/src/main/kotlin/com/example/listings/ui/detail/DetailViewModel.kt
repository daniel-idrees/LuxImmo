@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingDetailUseCase
import com.example.listings.models.SyncStatus
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.resource.ResourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * listingId is The ID of the listing to display
 */
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    private val getListingDetailUseCase: GetListingDetailUseCase,
    private val resourceProvider: ResourceProvider,
    @Assisted val listingId: Int,
) : ViewModel() {

    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)
    private var refreshJob: Job? = null

    /**
     * Observes the listing from the use case combined with sync status and maps it to view state.
     *
     * The stream is subscription-driven: collection starts when the UI subscribes and the initial
     * refresh is triggered via [onStart]. With [SharingStarted.WhileSubscribed] the upstream stops
     * 5s after the last subscriber leaves and restarts (re-refreshing) on the next subscription.
     */
    val viewState: StateFlow<DetailUiState> =
        combine(getListingDetailUseCase(listingId), syncStatus) { listing, status ->
            val errorConfig = calculateErrorConfig(listing, status)
            DetailUiState(
                listing = listing?.toListingUi(resourceProvider),
                isLoading = listing == null && errorConfig == null,
                error = errorConfig,
            )
        }
            .onStart { refreshListing() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailUiState(isLoading = true),
            )

    /**
     *  Determines error configuration based on listing availability and sync status
     *  No error if cached listing exists (prefer stale data over error)
     *  Shows sync error if no listing and sync failed
     *  Shows "not found" if no listing after successful sync
     */
    private fun calculateErrorConfig(listing: Listing?, syncStatus: SyncStatus): UiErrorConfig? {

        if (listing != null) {
            // Note: Future enhancement - notify the user of stale data when sync failed
            // Do nothing when cached data is avaible
            return null
        }

        // error screen when listing was not available for certain sync conditions
        return when (syncStatus) {
            is SyncStatus.Error -> UiErrorConfig(
                getErrorTextForErrorResult(syncStatus.error),
                onRetry = ::refreshListing
            )

            is SyncStatus.Finished -> UiErrorConfig(
                resourceProvider.getString(R.string.error_not_found)
            )

            else -> null
        }
    }

    /**
     * For explicit listing data refresh
     * Cancels the previous refresh if any and starts a new one
     */
    private fun refreshListing() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            syncStatus.emit(SyncStatus.Running)
            val result = getListingDetailUseCase.refresh(listingId)
            when (result) {
                is Result.Error -> syncStatus.emit(SyncStatus.Error(result.error))
                is Result.Success<*> -> {
                    syncStatus.emit(SyncStatus.Finished)
                }
            }
        }
    }

    private fun getErrorTextForErrorResult(error: AppError): String =
        when (error) {
            AppError.NoInternetConnection -> resourceProvider.getString(R.string.error_offline)
            AppError.Unknown -> resourceProvider.getString(R.string.error_unknown)
        }

    @AssistedFactory
    interface Factory {
        fun create(
            listingId: Int,
        ): DetailViewModel
    }
}
