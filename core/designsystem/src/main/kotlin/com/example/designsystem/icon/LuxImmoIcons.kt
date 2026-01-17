package com.example.designsystem.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.South
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import com.example.designsystem.util.ICON_SIZE_NORMAL


sealed class LuxImmoIcons(val imageVector: ImageVector, val contentDescription: String) {
    data object DownArrow : LuxImmoIcons(Icons.Default.South, "Down arrow icon for sort")
    data object Sort : LuxImmoIcons(Icons.AutoMirrored.Filled.Sort, "sort icon")
    data object Refresh : LuxImmoIcons(Icons.Default.Refresh, "Refresh icon on error")
    data object BackArrow : LuxImmoIcons(Icons.AutoMirrored.Filled.ArrowBack, "Back arrow icon")
    data object Close : LuxImmoIcons(Icons.Default.Close, "Close icon")
}

@Composable
fun LuxImmoIcon(
    luxImmoIcon: LuxImmoIcons,
    modifier: Modifier = Modifier,
    size: Int = ICON_SIZE_NORMAL,
    tint: Color = LocalContentColor.current,
) {
    Icon(
        modifier = modifier.size(size.dp),
        imageVector = luxImmoIcon.imageVector,
        contentDescription = luxImmoIcon.contentDescription,
        tint = tint
    )
}