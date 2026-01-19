@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.example.listings.ui.list

import androidx.lifecycle.viewModelScope
import com.example.common.di.DefaultDispatcher
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingsUseCase
import com.example.domain.util.NetworkMonitor
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

    private fun initialise() {
        loadData()
        observeAndHandleNetworkState()
    }

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

    private fun loadData() {
        val sortOptionFlow = viewState
            .mapLatest { it.activeSort }
            .distinctUntilChanged()

        val sortedListingsFlow =
            combine(getListingsUseCase(), sortOptionFlow) { listings, sortOption ->
                listings
                    .sort(sortOption)
                    .map { it.toListingUi(resourceProvider = resourceProvider) }
            }.flowOn(defaultDispatcher)

        combine(sortedListingsFlow, syncResult) { list, result ->
            val errorConfig: UiErrorConfig? =
                if (list.isEmpty() && result is SyncStatus.Error) getErrorConfig(error = result.error) else null

            Triple(list, result, errorConfig)
        }
            .onStart { refreshData() }
            .onEach { (list, result, errorConfig) ->

                // list is available but sync failed
                if(list.isNotEmpty() && result is SyncStatus.Error) {
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