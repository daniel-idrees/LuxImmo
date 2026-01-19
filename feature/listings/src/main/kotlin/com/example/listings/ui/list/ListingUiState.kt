package com.example.listings.ui.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.core.ui.R
import com.example.listings.models.ListingUi
import com.example.ui.models.UiErrorConfig
import com.example.ui.mvi.ViewState

@Immutable
internal data class ListingUiState(
    val isLoading: Boolean = false,
    val listings: List<ListingUi> = emptyList(),
    val selectedListing: ListingUi? = null,
    val errorConfig: UiErrorConfig? = null,
    val activeSort: ListingSortOption = ListingSortOption.Default,
    val isRefreshing: Boolean = false
) : ViewState



internal enum class ListingSortOption(@StringRes val labelRes: Int) {
    Default(R.string.sorting_option_default),
    PriceAsc(R.string.sorting_option_price_asc),
    PriceDesc(R.string.sorting_option_price_desc),
    PricePerSquareMeterAsc(R.string.sorting_option_price_per_square_meter_asc),
    PricePerSquareMeterDesc(R.string.sorting_option_price_per_square_meter_desc)
}