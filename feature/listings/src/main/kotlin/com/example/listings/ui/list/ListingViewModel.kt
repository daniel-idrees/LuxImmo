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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ListingViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getListingsUseCase: GetListingsUseCase,
    private val networkMonitor: NetworkMonitor,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : MviViewModel<ListingUiAction, ListingUiState, ListingUiEffect>() {

    private val syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.NotStarted)
    private var refreshJob: Job? = null

    private fun initialise() {
        observeListingsAndSync()
        observeAndHandleNetworkState()
    }

    override fun setInitialState(): ListingUiState = ListingUiState(isLoading = true)

    override fun handleAction(event: ListingUiAction) {
        when (event) {
            ListingUiAction.Init -> initialise()
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
                    if (syncStatus.value is SyncStatus.Error) {
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
     * Observes listings from use case, combines with sort options and sync status.
     *
     * Key behaviors:
     * - Auto-refreshes on initialization
     * - Reacts to sort option changes without re-fetching
     * - Shares upstream execution to prevent duplicate work
     * - Shows error effects only when cached data exists
     */
    private fun observeListingsAndSync() {
        // keep a track on sort option in the view state
        val sortOptionFlow = viewState
            .mapLatest { it.activeSort }
            .distinctUntilChanged()

        //keep a track on listings from use case and current sort option, then sort the list accordingly
        val sortedListingsFlow =
            getListingsUseCase()
                .distinctUntilChanged()
                .combine(sortOptionFlow) { listings, sortOption ->
                    listings
                        .map { it.toListingUi(resourceProvider = resourceProvider) }
                        .sortBy(sortOption)
                }
                .flowOn(defaultDispatcher)

        //keep a track on the final list and sync result
        val combinedDataFlow = combine(sortedListingsFlow, syncStatus) { list, status ->
            val uiErrorConfig: UiErrorConfig? = calculateErrorConfig(listings = list, status = status)

            ListingViewData(listings = list, syncStatus = status, uiErrorConfig = uiErrorConfig)
        }
            .onStart { refreshData() } // Start refreshing so that local storage can be updated
            .shareIn(      //sharing to prevent multiple executions
                scope = viewModelScope,
                started = WhileSubscribed(5000),
                replay = 1
            )

        // Collection for view state updates
        combinedDataFlow
            .onEach { data ->
                setState {
                    copy(
                        listings = data.listings,
                        errorConfig = data.uiErrorConfig,
                        isRefreshing = data.syncStatus is SyncStatus.Running,
                        isLoading = data.listings.isEmpty() && data.syncStatus is SyncStatus.Running
                    )
                }
            }.launchIn(viewModelScope)

        // Collection for view effects when sync status is changed to error
        combinedDataFlow
            .distinctUntilChangedBy { it.syncStatus }
            .filter { it.listings.isNotEmpty() && it.syncStatus is SyncStatus.Error }
            .onEach { data ->
                handleErrorEffect((data.syncStatus as SyncStatus.Error).error)
            }
            .launchIn(viewModelScope)
    }

    private data class ListingViewData(
        val listings: List<ListingUi>,
        val syncStatus: SyncStatus,
        val uiErrorConfig: UiErrorConfig?
    )

    private fun calculateErrorConfig(
        listings: List<ListingUi>,
        status: SyncStatus
    ): UiErrorConfig? =
        when {
            listings.isNotEmpty() -> null // Has cached data, don't show error screen
            status is SyncStatus.Error -> UiErrorConfig(
                errorText = getErrorText(status.error),
                onRetry = ::refreshData
            )

            else -> null
        }

    private fun getErrorText(error: AppError): String {
        val errorTextRes = when (error) {
            is AppError.NoInternetConnection -> R.string.error_offline
            is AppError.Unknown -> R.string.error_unknown
        }

        return resourceProvider.getString(errorTextRes)
    }


    private fun List<ListingUi>.sortBy(sortOption: ListingSortOption): List<ListingUi> =
        when (sortOption) {
            ListingSortOption.Default -> this
            ListingSortOption.PriceAsc -> this.sortedBy { it.price.value }
            ListingSortOption.PriceDesc -> this.sortedByDescending { it.price.value }
            // After asc price per meter value, sort by price if same price per meter value
            ListingSortOption.PricePerSquareMeterAsc -> this.sortedWith(
                compareBy<ListingUi> { it.pricePerSquareMeter.value }
                    .thenBy { it.price.value })
            // After desc price per meter value, sort desc by price if same price per meter value
            ListingSortOption.PricePerSquareMeterDesc -> this.sortedWith(
                compareByDescending<ListingUi> { it.pricePerSquareMeter.value }
                    .thenByDescending { it.price.value }
            )
        }

    /**
     * For explicit data refresh
     * Cancels the previous refresh if any and starts a new one
     */
    private fun refreshData() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {

            launch {
                syncStatus.emit(SyncStatus.Running)

                val result = getListingsUseCase.refresh()
                when (result) {
                    is Result.Error -> syncStatus.emit(SyncStatus.Error(result.error))
                    is Result.Success<*> -> syncStatus.emit(SyncStatus.Finished)
                }
            }
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