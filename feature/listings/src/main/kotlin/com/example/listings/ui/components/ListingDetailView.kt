package com.example.listings.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.listings.models.ListingUi
import com.example.ui.helper.SPACING_MEDIUM
import com.example.ui.helper.SPACING_SMALL

@Composable
internal fun ListingDetailView(
    modifier: Modifier = Modifier,
    listing: ListingUi,
    imageModifier: Modifier = Modifier,
    imageContentScale: ContentScale = ContentScale.Fit,
    isSelected: Boolean = false
) {

    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 1.dp,
        label = "BorderWidthAnimation"
    )

    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.LightGray.copy(alpha = 0.5f)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(SPACING_MEDIUM.dp),
        border = BorderStroke(width = borderWidth, color = borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
        ) {

            ListingImageView(
                modifier = imageModifier,
                imageUrl = listing.imageUrl,
                contentScale = imageContentScale
            )

            ListingDetailContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SPACING_MEDIUM.dp),
                listing = listing
            )

            VendorBannerView(vendor = listing.vendor)
        }
    }
}