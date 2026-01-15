package com.example.listings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ListingScreen(
    listingViewModel: ListingViewModel = hiltViewModel()
) {
}

@Composable
@PreviewLightDark
private fun ListingPreview() {
    ListingScreen()
}