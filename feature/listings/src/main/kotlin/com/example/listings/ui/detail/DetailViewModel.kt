@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.usecase.GetListingDetailUseCase
import com.example.listings.models.toListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.resource.ResourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    private val getListingDetailUseCase: GetListingDetailUseCase,
    private val resourceProvider: ResourceProvider,
    @Assisted val listingId: Int,
) : ViewModel() {
    private val _error = Channel<String>(capacity = Channel.BUFFERED)
    val errorEffect = _error.receiveAsFlow()

    private val syncResult = MutableStateFlow<Result<Listing>?>(null)
    val viewState: StateFlow<DetailUiState> =
        combine(
            getListingDetailUseCase(listingId),
            syncResult
        ) { listing, result ->
            when {
                listing != null -> {
                    when {
                        result is Result.Error -> {
                            //TODO think something how to show user that data is cached
                        }

                        result is Result.Success -> {
                            //TODO think something remove any cached indication
                        }
                    }

                    DetailUiState(
                        listing = listing.toListingUi(resourceProvider),
                        isLoading = false
                    )
                }

                result is Result.Error -> {
                    DetailUiState(
                        isLoading = false,
                        error = UiErrorConfig(
                            getErrorTextForErrorResult(result),
                            onRetry = ::refreshListing
                        )
                    )
                }

                else -> {
                    DetailUiState(isLoading = true) // keep waiting for the data
                }
            }
        }
            .onStart {
                refreshListing()
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DetailUiState(isLoading = true)
            )

    private fun refreshListing() {
        viewModelScope.launch {
            val result = getListingDetailUseCase.refresh(listingId)
            syncResult.emit(result)
        }
    }

    private fun getErrorTextForErrorResult(result: Result.Error): String {
        return when (result.error) {
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