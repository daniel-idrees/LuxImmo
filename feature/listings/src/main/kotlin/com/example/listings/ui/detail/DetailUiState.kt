package com.example.listings.ui.detail

import com.example.listings.models.ListingUi

internal data class DetailUiState(
    val isLoading: Boolean = false,
    val listing: ListingUi? = null,
    val error: String? = null,
)