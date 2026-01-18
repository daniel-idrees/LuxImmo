@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.common.di.DefaultDispatcher
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.SyncStatus
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ListingViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getListingsUseCase: GetListingsUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    private val syncResult = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)

    private val sortOptionFlow = viewState
        .mapLatest { it.activeSort }
        .distinctUntilChanged()

    override fun setInitialState(): ListingUiState = ListingUiState()

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> loadData()
            ListingUiAction.Refresh -> refreshData()
            is ListingUiAction.OnListingClick -> {
                setState {
                    copy(
                        selectedListing = event.listing
                    )
                }
                setEffect {
                    ListingUiEffect.NavigateToDetail(event.listing.id)
                }
            }

            is ListingUiAction.OnSortChange -> {
                setState { copy(activeSort = event.newSort, isLoading = true) }
            }

            ListingUiAction.RemoveSelection -> {
                setState {
                    copy(
                        selectedListing = null
                    )
                }
            }
        }
    }

    private fun loadData() {

        setState { copy(isLoading = true) }

        combine(
            getListingsUseCase(),
            syncResult,
            sortOptionFlow
        ) { listings, result, sortOption ->

            val sortedList = listings
                .sort(sortOption)
                .map {
                    it.toListingUi(
                        resourceProvider = resourceProvider
                    )
                }

            Pair(sortedList, result)
        }
            .flowOn(defaultDispatcher)
            .onStart {
                refreshData()
            }.onEach { (list, result) ->
                if (list.isNotEmpty() && result is SyncStatus.Error) {
                    handleError(error = result.error)
                }
                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = result is SyncStatus.Running,
                    )
                }
            }
            .mapLatest { it.first }
            .distinctUntilChanged()
            .onEach { list ->
                setState {
                    copy(
                        listings = list
                    )
                }
            }.launchIn(viewModelScope)

    }

    private fun List<Listing>.sort(sortOption: ListingSortOption): List<Listing> =
        when (sortOption) {
            ListingSortOption.Default -> this
            ListingSortOption.PriceAsc -> this.sortedBy { it.price }
            ListingSortOption.PriceDesc -> this.sortedByDescending { it.price }
            ListingSortOption.Area -> this.sortedBy { it.area }
        }

    /**
     * For explicit data refresh
     */
    private fun refreshData() {
        viewModelScope.launch {
            syncResult.emit(SyncStatus.Running)

            setState {
                copy(
                    isLoading = viewState.value.listings == null,
                    isRefreshing = true
                )
            }

            val result = getListingsUseCase.refresh()
            when(result) {
                is Result.Error -> syncResult.emit(SyncStatus.Error(result.error))
                is Result.Success<*> -> syncResult.emit(SyncStatus.Finished)
            }
        }
    }

    private fun handleError(error: AppError) {
        when (error) {
            is AppError.NoInternetConnection -> handleNoInternetError()
            is AppError.Unknown -> handleUnknownError()
        }
    }

    private fun handleNoInternetError() {
        val currentListing = viewState.value.listings
        if (currentListing == null || currentListing.isEmpty()) {
            setState {
                copy(
                    errorConfig = UiErrorConfig(
                        errorText = resourceProvider.getString(
                            R.string.error_offline
                        ),
                        onRetry = ::refreshData
                    )
                )
            }
        } else {
            setEffect {
                ListingUiEffect.ShowSnackbar(
                    message = resourceProvider.getString(R.string.error_offline)
                )
            }
        }
    }

    private fun handleUnknownError() {
        val currentListing = viewState.value.listings
        if (currentListing == null || currentListing.isEmpty()) {
            setState {
                copy(
                    errorConfig = UiErrorConfig(
                        errorText = resourceProvider.getString(
                            R.string.error_unknown
                        ),
                        onRetry = {
                            refreshData()
                        }
                    )
                )
            }
        } else {
            setEffect {
                ListingUiEffect.ShowSnackbar(
                    message = resourceProvider.getString(R.string.error_unknown)
                )
            }
        }
    }
}