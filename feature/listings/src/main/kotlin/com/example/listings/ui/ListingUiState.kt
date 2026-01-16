package com.example.listings.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.core.ui.R
import com.example.listings.models.ListingUi
import com.example.ui.mvi.ViewState

@Immutable
internal data class ListingUiState(
    val isLoading: Boolean = false,
    val listings: List<ListingUi> = emptyList(),
    val selectedListing: ListingUi? = null,
    val error: String? = null,
    val activeSort: ListingSortOption = ListingSortOption.Default
) : ViewState



internal enum class ListingSortOption(@StringRes val labelRes: Int) {
    Default(R.string.sorting_option_default),
    PriceAsc(R.string.sorting_option_price_asc),
    PriceDesc(R.string.sorting_option_price_desc),
    Area(R.string.sorting_option_area)
}