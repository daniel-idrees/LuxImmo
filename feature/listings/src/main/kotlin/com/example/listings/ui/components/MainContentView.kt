package com.example.listings.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.designsystem.util.SPACING_SMALL
import com.example.ui.components.GenericErrorView
import com.example.ui.components.LoadingView
import com.example.ui.models.UiErrorConfig

/**
 * Main content container that handles loading and error states.
 * Main content is only shown if the state is not loading or error.
 */
@Composable
internal fun MainContentView(
    isLoading: Boolean,
    errorConfig: UiErrorConfig?,
    content: @Composable () -> Unit,
) {
    when {
        isLoading -> {
            LoadingView(
                modifier = Modifier.fillMaxSize()
            )
        }

        errorConfig != null ->
            GenericErrorView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SPACING_SMALL.dp),
                errorText = errorConfig.errorText,
                onRetry = errorConfig.onRetry
            )

        else -> content()
    }
}