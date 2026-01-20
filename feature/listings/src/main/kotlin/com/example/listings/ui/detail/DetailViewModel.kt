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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    private val getListingDetailUseCase: GetListingDetailUseCase,
    private val resourceProvider: ResourceProvider,
    @Assisted val listingId: Int,
) : ViewModel() {
    private val syncResult = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)

    val viewState: StateFlow<DetailUiState>
        field = MutableStateFlow(DetailUiState(isLoading = true))

    // Do it from view to have control
    fun initialise() {
        loadData()
    }

    /**
     * Start collecting data from use case and observes sync result
     * Also start refreshing the data in the beginning only once
     * Updates view state on the basis of listing availability and sync result
     */
    private fun loadData() {
        combine(
            getListingDetailUseCase(listingId),
            syncResult,
        ) { listing, result ->
            // calculateError will decide whether to show error or not
            val errorConfig = calculateError(listing, result)
            Pair(listing, errorConfig)
        }
            .onStart {
                refreshListing()
            }
            .onEach { (listing, errorConfig) ->
                // loader shows when no listing is available and
                // there is no error either, which means sync is not completed
                viewState.update {
                    it.copy(
                        listing = listing?.toListingUi(resourceProvider),
                        isLoading = listing == null && errorConfig == null,
                        error = errorConfig
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     *  We avoid showing error if the listing is available
     *  We only show error when listing is not available for
     *  either sync failure or sync finished case (when listing is still not available)
     */
    private fun calculateError(listing: Listing?, result: SyncStatus): UiErrorConfig? {

        if(listing != null) {
            // TODO when list is available but result is SyncStatus.Error, notify the user that data is cached

            // For now, Do not show error when listing is present
            return null
        }

        // error screen when listing was not available for certain sync conditions
        return when (result) {
            is SyncStatus.Error -> UiErrorConfig(
                getErrorTextForErrorResult(result.error),
                onRetry = ::refreshListing
            )


            is SyncStatus.Finished -> UiErrorConfig(
                resourceProvider.getString(R.string.error_not_found)
            )

            else -> null
        }
    }

    private fun refreshListing() {
        viewModelScope.launch {
            syncResult.emit(SyncStatus.Running)
            val result = getListingDetailUseCase.refresh(listingId)
            when (result) {
                is Result.Error -> syncResult.emit(SyncStatus.Error(result.error))
                is Result.Success<*> -> {
                    syncResult.emit(SyncStatus.Finished)
                }
            }
        }
    }

    private fun getErrorTextForErrorResult(error: AppError): String {
        return when (error) {
            AppError.NoInternetConnection -> resourceProvider.getString(R.string.error_offline)
            AppError.Unknown -> resourceProvider.getString(R.string.error_unknown)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            listingId: Int,
        ): DetailViewModel
    }
}