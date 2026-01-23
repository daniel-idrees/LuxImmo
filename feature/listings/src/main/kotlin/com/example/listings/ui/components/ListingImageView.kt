package com.example.listings.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.DynamicAsyncImage

@Composable
internal fun ListingImageView(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentScale: ContentScale = ContentScale.Fit,
) {
    DynamicAsyncImage(
        imageUrl = imageUrl,
        contentDescription = "Listing Image",
        modifier = modifier,
        contentScale = contentScale
    )
}