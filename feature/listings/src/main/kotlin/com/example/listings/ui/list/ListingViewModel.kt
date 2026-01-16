package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.toListingUi
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
                setState { copy(isLoading = true) }
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
                        is Result.Error -> handleErrorForSilentSync(it)
                        is Result.Success -> {
                            // notify user that data is latest
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
            getListingsUseCase()
                .onStart {
                    setState { copy(isLoading = true) }
                }
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
                    setState {
                        copy(
                            isLoading = false,
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
            val result = getListingsUseCase.refresh()
            when (result) {
                is Result.Error -> handleErrorForManualSync(result)
                Result.Success -> {
                    setState { copy(isLoading = false) }
                }
            }
        }
    }

    private fun handleErrorForManualSync(error: Result.Error) {
        when (error) {
            Result.Error.NoInternetConnection -> handleNoInternetError()
            Result.Error.Unknown -> {
                if (viewState.value.listings.isEmpty()) {
                    setState {
                        copy(
                            isLoading = false,
                            error = resourceProvider.getString(R.string.error_unknown)
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
    }

    private fun handleErrorForSilentSync(error: Result.Error) {
        when (error) {
            is Result.Error.Unknown -> {
                if (viewState.value.listings.isEmpty()) {
                    setState {
                        copy(
                            error = resourceProvider.getString(R.string.error_unknown)
                        )
                    }
                }
            }

            is Result.Error.NoInternetConnection -> handleNoInternetError()
        }
    }

    private fun handleNoInternetError() {
        setEffect {
            ListingUiEffect.ShowSnackbar(
                message = resourceProvider.getString(R.string.error_not_internet)
            )
        }

        if (viewState.value.listings.isEmpty()) {
            setState {
                copy(
                    error = resourceProvider.getString(R.string.error_not_internet)
                )
            }
        }
    }
}