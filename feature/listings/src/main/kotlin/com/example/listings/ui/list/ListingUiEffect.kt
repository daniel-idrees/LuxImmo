package com.example.listings.ui.list

import com.example.ui.mvi.ViewSideEffect

internal sealed interface ListingUiEffect: ViewSideEffect {
    data class ShowSnackbar(val message: String): ListingUiEffect
    data class NavigateToDetail(val id: String): ListingUiEffect
}