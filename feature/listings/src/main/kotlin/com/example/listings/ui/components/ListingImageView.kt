package com.example.listings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
internal fun ListingImageView(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Box(
        modifier = modifier
            .background(Color.LightGray)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize(),
            model = imageUrl,
            contentDescription = "Listing Image",
            contentScale = contentScale,
        )
    }
}