@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.example.designsystem.icon.LuxImmoIcon
import com.example.designsystem.icon.LuxImmoIcons
import com.example.designsystem.util.ICON_SIZE_LARGE


@Composable
fun SimpleAppBar(
    title: String = "",
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                LuxImmoIcon(
                    luxImmoIcon = LuxImmoIcons.BackArrow,
                    size = ICON_SIZE_LARGE
                )
            }
        },
     //   colors = TopAppBarDefaults.topAppBarColors(
     //       containerColor = MaterialTheme.colorScheme.primaryContainer,
     //       titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      //  )
    )
}