package com.example.listings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.helper.SPACING_MEDIUM

@Composable
internal fun VendorBannerView(vendor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = SPACING_MEDIUM.dp),
            text = vendor,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}