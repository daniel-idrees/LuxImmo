package com.example.listings.ui

import com.example.ui.mvi.ViewSideEffect

internal sealed interface ListingUiEffect: ViewSideEffect {
    data class ShowSnackbar(val message: String): ListingUiEffect
    data class NavigateToDetail(val id: String): ListingUiEffect
}