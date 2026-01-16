package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.listings.models.toListingUi
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ListingViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getListingsUseCase: GetListingsUseCase
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    private val listings by lazy {
        getListingsUseCase()
    }

    override fun setInitialState(): ListingUiState = ListingUiState()

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> {
                loadData()
                refreshData()
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

        val sortOptionFlow = viewState
            .mapLatest { it.activeSort }
            .distinctUntilChanged()

        viewModelScope.launch {
            listings
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
            ListingSortOption.PriceDesc -> this.sortedByDescending { it.price}
            ListingSortOption.Area -> this.sortedBy { it.area }
        }


    private fun refreshData() {
        viewModelScope.launch {
            val result = getListingsUseCase.refresh()
            when (result) {
                is Result.Error -> {
                    if (viewState.value.listings.isEmpty()) {
                        setState {
                            copy(
                                isLoading = false,
                                error = "Something went wrong"   //handle inter error
                            )
                        }
                    } else {
                        setEffect {
                            ListingUiEffect.ShowSnackbar(
                                message = "Something went wrong"
                            )
                        }
                    }
                }

                Result.Success -> {
                    setState { copy(isLoading = false) }
                }
            }
        }
    }
}