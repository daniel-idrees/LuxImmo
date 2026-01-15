package com.example.detail

import androidx.lifecycle.ViewModel
import com.example.domain.usecase.GetListingDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    private val getListingDetailUseCase: GetListingDetailUseCase,
    @Assisted val listingId: String,
): ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            listingId: String,
        ): DetailViewModel
    }
}