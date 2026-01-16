package com.example.listings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.listings.models.ListingUi
import com.example.designsystem.util.SPACING_SMALL

@Composable
internal fun ListingDetailContent(
    modifier: Modifier = Modifier,
    listing: ListingUi
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = listing.price.formatted,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = listing.pricePerSquareMeter.formatted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text(
            text = listing.propertyType,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
        ) {
            listing.rooms?.let {
                Text(
                    text = it.formatted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            listing.bedrooms?.let {
                Text(
                    text = it.formatted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                text = listing.area.formatted,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Text(
            text = listing.city,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}