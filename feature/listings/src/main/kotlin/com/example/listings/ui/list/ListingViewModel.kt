@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ListingViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getListingsUseCase: GetListingsUseCase
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    override fun setInitialState(): ListingUiState = ListingUiState()

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> {
                observeRefreshResult()
                loadData()
            }

            ListingUiAction.Refresh -> {
                refreshData()
            }

            is ListingUiAction.OnListingClick -> {
                setState {
                    copy(
                        selectedListing = event.listing
                    )
                }
                setEffect {
                    ListingUiEffect.NavigateToDetail(event.listing.id.toString())
                }
            }

            is ListingUiAction.OnSortChange -> {
                setState { copy(activeSort = event.newSort) }
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

    private fun observeRefreshResult() {
        viewModelScope.launch {
            getListingsUseCase.refreshResultEvent
                .collectLatest {
                    when (it) {
                        is Result.Error -> handleRefreshError(it)
                        is Result.Success -> {
                            // notify user that data is latest
                            setState {
                                copy(
                                    isRefreshing = false,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun loadData() {
        val sortOptionFlow = viewState
            .mapLatest { it.activeSort }
            .distinctUntilChanged()
            .onEach {
                setState { copy(isLoading = true) }
            }

        viewModelScope.launch {
            setState { copy(isLoading = true) }

            getListingsUseCase()
                .distinctUntilChanged()
                .combine(sortOptionFlow) { list, sortOption ->
                    val sortedList = list.sort(sortOption)
                    sortedList.map {
                        it.toListingUi(
                            resourceProvider = resourceProvider
                        )
                    }
                }
                .collect { list ->
                    if (list.isNotEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                errorConfig = null,
                                listings = list
                            )
                        }
                    }
                }
        }
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
            if (viewState.value.listings.isEmpty()) {
                setState { copy(isLoading = true) }
            } else {
                setState { copy(isRefreshing = true) }
            }

            getListingsUseCase.refresh()
        }
    }

    private fun handleRefreshError(error: Result.Error) {
        when (error) {
            is Result.Error.NoInternetConnection -> handleNoInternetError()

            is Result.Error.Unknown -> {
                if (viewState.value.listings.isEmpty()) {
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
                } else  {
                    setEffect {
                        ListingUiEffect.ShowSnackbar(
                            message = resourceProvider.getString(R.string.error_unknown)
                        )
                    }
                }

                setState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }
        }
    }

    private fun handleNoInternetError() {
        if (viewState.value.listings.isEmpty()) {
            setState {
                copy(
                    errorConfig = UiErrorConfig(
                        errorText = resourceProvider.getString(
                            R.string.error_not_internet
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
                    message = resourceProvider.getString(R.string.error_not_internet)
                )
            }
        }

        setState {
            copy(
                isLoading = false,
                isRefreshing = false,
            )
        }
    }
}