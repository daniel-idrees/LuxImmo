@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.listings.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.ui.R
import com.example.domain.usecase.GetListingDetailUseCase
import com.example.listings.models.toListingUi
import com.example.ui.resource.ResourceProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    getListingDetailUseCase: GetListingDetailUseCase,
    private val resourceProvider: ResourceProvider,
    @Assisted val listingId: String,
) : ViewModel() {

    val viewState: StateFlow<DetailUiState> =
        getListingDetailUseCase(listingId.toInt())
            .onStart {
                DetailUiState(isLoading = true)
            }
            .distinctUntilChanged()
            .catch {
                DetailUiState(error = it.message)
            }
            .mapLatest {
                if (it != null) {
                    DetailUiState(listing = it.toListingUi(resourceProvider), isLoading = false, error = null)
                } else {
                    DetailUiState(error = resourceProvider.getString(R.string.error_unknown))
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DetailUiState()
            )


    @AssistedFactory
    interface Factory {
        fun create(
            listingId: String,
        ): DetailViewModel
    }
}