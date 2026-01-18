package com.example.listings.ui.detail

import androidx.compose.runtime.Immutable
import com.example.listings.models.ListingUi
import com.example.ui.models.UiErrorConfig

@Immutable
internal data class DetailUiState(
    val isLoading: Boolean = false,
    val listing: ListingUi? = null,
    val error: UiErrorConfig? = null,
)