package com.example.designsystem.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
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
}

@Composable
fun LuxImmoIcon(
    luxImmoIcon: LuxImmoIcons,
    modifier: Modifier = Modifier,
    size: Int = ICON_SIZE_NORMAL,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = luxImmoIcon.imageVector,
        contentDescription = luxImmoIcon.contentDescription,
        modifier = modifier.size(size.dp),
        tint = tint
    )
}