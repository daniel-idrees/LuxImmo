@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.exception.ListingsUnavailableException
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ListingViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getListingsUseCase: GetListingsUseCase,
    //private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    private val sortOptionFlow = viewState
        .mapLatest { it.activeSort }
        .distinctUntilChanged()
        .onEach {
            setState { copy(isLoading = true) }
        }

    override fun setInitialState(): ListingUiState = ListingUiState()

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> {
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
                    ListingUiEffect.NavigateToDetail(event.listing.id)
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

    private fun loadData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            getListingsUseCase()
                .distinctUntilChanged()
                .catch {
                    if (it is ListingsUnavailableException) {
                        handleError(it.errorResult)
                    } else {
                        handleUnknownError()
                    }
                }
                .combine(sortOptionFlow) { list, sortOption ->
                    val sortedList = list.sort(sortOption)
                    sortedList.map {
                        it.toListingUi(
                            resourceProvider = resourceProvider
                        )
                    }
                }
                .flowOn(Dispatchers.Default) //for sorting
                .collect { list ->
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
            //If no data available, show big loader else only keep refresher indicator
            if (viewState.value.listings == null) {
                setState { copy(isLoading = true) }
            } else {
                setState { copy(isRefreshing = true) }
            }

            val result = getListingsUseCase.refresh()

            when (result) {
                is Result.Error -> handleError(result)
                is Result.Success -> {
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

    private fun handleError(errorResult: Result.Error) {
        when (errorResult.error) {
            is AppError.NoInternetConnection -> handleNoInternetError()
            is AppError.Unknown -> handleUnknownError()
        }

        setState {
            copy(
                isLoading = false,
                isRefreshing = false,
            )
        }
    }

    private fun handleNoInternetError() {
        val currentListing = viewState.value.listings
        if (currentListing == null || currentListing.isEmpty()) {
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
    }
    private fun handleUnknownError(){
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