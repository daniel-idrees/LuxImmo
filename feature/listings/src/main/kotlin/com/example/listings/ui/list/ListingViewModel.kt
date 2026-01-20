@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.common.di.DefaultDispatcher
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.usecase.GetListingsUseCase
import com.example.domain.util.NetworkMonitor
import com.example.listings.models.ListingUi
import com.example.listings.models.SyncStatus
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.mvi.MviViewModel
import com.example.ui.resource.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    private val syncResult = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)

    private fun initialise() {
        loadData()
        observeAndHandleNetworkState()
    }

    override fun setInitialState(): ListingUiState = ListingUiState(isLoading = true)

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> initialise()
            ListingUiAction.Refresh -> {
                setState {
                    copy(
                        isRefreshing = true
                    )
                }
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

            is ListingUiAction.OnSortChange -> setState { copy(activeSort = event.newSort) }
            ListingUiAction.RemoveSelection -> setState { copy(selectedListing = null) }
        }
    }

    /**
     * It observes network status, waits for 1sec
     * Starts refreshing the data only if the internet is back and sync failed previously
     */
    private fun observeAndHandleNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .debounce(1000)
                .filter { isOnline -> isOnline }
                .collectLatest {
                    if (syncResult.value is SyncStatus.Error) {
                        setState {
                            copy(
                                isRefreshing = true
                            )
                        }
                        refreshData()
                    }
                }
        }
    }


    /**
     * Start refreshing the data in the beginning only once
     * Start collecting data from use case and sort option in the view state
     * combines use case result and sort option to sort the list result
     * Combines the sorted list with the sync result to prepare the view state
     * Updates view state on the basis of listing availability and sync result
     * If the listing was available not sync failed, error effect will be sent
     */
    private fun loadData() {
        val sortOptionFlow = viewState
            .mapLatest { it.activeSort }
            .distinctUntilChanged()

        val sortedListingsFlow =
            combine(getListingsUseCase(), sortOptionFlow) { listings, sortOption ->
                listings
                    .map { it.toListingUi(resourceProvider = resourceProvider) }
                    .sort(sortOption)
            }.flowOn(defaultDispatcher)

        combine(sortedListingsFlow, syncResult) { list, result ->
            val errorConfig: UiErrorConfig? =
                if (list.isEmpty() && result is SyncStatus.Error) getErrorConfig(error = result.error) else null

            Triple(list, result, errorConfig)
        }
            .onStart { refreshData() }
            .onEach { (list, result, errorConfig) ->

                // list is available but sync failed
                if (list.isNotEmpty() && result is SyncStatus.Error) {
                    handleErrorEffect(error = result.error)
                }

                setState {
                    copy(
                        listings = list,
                        errorConfig = errorConfig,
                        isRefreshing = result is SyncStatus.Running,
                        isLoading = list.isEmpty() && result is SyncStatus.Running
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun List<ListingUi>.sort(sortOption: ListingSortOption): List<ListingUi> =
        when (sortOption) {
            ListingSortOption.Default -> this
            ListingSortOption.PriceAsc -> this.sortedBy { it.price.value }
            ListingSortOption.PriceDesc -> this.sortedByDescending { it.price.value }
            // After asc price per meter value, sort by price if same price per meter value
            ListingSortOption.PricePerSquareMeterAsc -> this.sortedWith(
                compareBy<ListingUi> { it.pricePerSquareMeter.value }
                    .thenBy { it.pricePerSquareMeter.value })
            // After desc price per meter value, sort desc by price if same price per meter value
            ListingSortOption.PricePerSquareMeterDesc -> this.sortedWith(
                compareByDescending<ListingUi> { it.pricePerSquareMeter.value }
                    .thenByDescending { it.pricePerSquareMeter.value }
            )
        }

    /**
     * For explicit data refresh
     */
    private fun refreshData() {
        viewModelScope.launch {
            syncResult.emit(SyncStatus.Running)

            val result = getListingsUseCase.refresh()
            when (result) {
                is Result.Error -> syncResult.emit(SyncStatus.Error(result.error))
                is Result.Success<*> -> syncResult.emit(SyncStatus.Finished)
            }
        }
    }

    private fun getErrorConfig(error: AppError): UiErrorConfig? {
        return when (error) {
            is AppError.NoInternetConnection -> return UiErrorConfig(
                errorText = resourceProvider.getString(
                    R.string.error_offline
                ),
                onRetry = ::refreshData
            )

            is AppError.Unknown -> UiErrorConfig(
                errorText = resourceProvider.getString(
                    R.string.error_unknown
                ),
                onRetry = ::refreshData
            )
        }
    }

    private fun handleErrorEffect(error: AppError) {
        when (error) {
            is AppError.NoInternetConnection -> setEffect {
                ListingUiEffect.ShowSnackbar(
                    message = resourceProvider.getString(R.string.error_offline)
                )
            }

            is AppError.Unknown -> setEffect {
                ListingUiEffect.ShowSnackbar(
                    message = resourceProvider.getString(R.string.error_unknown)
                )
            }
        }
    }
}