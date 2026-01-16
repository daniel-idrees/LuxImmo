package com.example.listings.ui

import com.example.listings.models.ListingUi
import com.example.ui.mvi.ViewAction

internal sealed interface ListingUiAction: ViewAction {
    data class OnListingClick(val listing: ListingUi): ListingUiAction
    data object Init: ListingUiAction
    data object Refresh: ListingUiAction
    data object RemoveSelection : ListingUiAction
    data class OnSortChange(val newSort: ListingSortOption): ListingUiAction
}
