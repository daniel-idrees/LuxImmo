@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.AppError
import com.example.domain.exception.ListingDetailUnavailableException
import com.example.domain.usecase.GetListingDetailUseCase
import com.example.listings.models.toListingUi
import com.example.ui.resource.ResourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    getListingDetailUseCase: GetListingDetailUseCase,
    private val resourceProvider: ResourceProvider,
    @Assisted val listingId: Int,
) : ViewModel() {
    private val _error = Channel<String>(capacity = Channel.BUFFERED)
    val errorEffect = _error.receiveAsFlow()

    val viewState: StateFlow<DetailUiState> =
        getListingDetailUseCase(listingId)
            .distinctUntilChanged()
            .mapLatest {
                DetailUiState(listing = it.toListingUi(resourceProvider), isLoading = false)
            }
            .catch {
                val errorMessage =
                    if (it is ListingDetailUnavailableException && it.errorResult.error is AppError.NoInternetConnection) {
                        resourceProvider.getString(R.string.error_not_internet)
                    } else {
                        resourceProvider.getString(R.string.error_unknown)
                    }
                _error.send(errorMessage)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DetailUiState(isLoading = true)
            )

    @AssistedFactory
    interface Factory {
        fun create(
            listingId: Int,
        ): DetailViewModel
    }
}