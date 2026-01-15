package com.example.listings

import androidx.lifecycle.ViewModel
import com.example.domain.usecase.GetListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val listingsUseCase: GetListingsUseCase
) : ViewModel() {

}