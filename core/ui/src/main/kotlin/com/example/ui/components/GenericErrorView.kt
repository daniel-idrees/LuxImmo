package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.icon.LuxImmoIcon
import com.example.designsystem.icon.LuxImmoIcons
import com.example.designsystem.util.ICON_SIZE_LARGE
import com.example.designsystem.util.SPACING_SMALL

@Composable
fun GenericErrorView(
    modifier: Modifier,
    errorText: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SPACING_SMALL.dp)
        ) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.onSurface
            )
            LuxImmoIcon(
                luxImmoIcon = LuxImmoIcons.Refresh,
                modifier = Modifier
                    .clickable {
                        onRetry()
                    },
                size = ICON_SIZE_LARGE,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}