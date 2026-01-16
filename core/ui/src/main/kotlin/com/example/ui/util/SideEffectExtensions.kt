package com.example.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.ui.mvi.ViewSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects [SideEffect] from a [Flow] in a lifecycle-aware manner.
 *
 * Source: https://github.com/philipplackner/CryptoTracker/blob/master/app/src/main/java/com/plcoding/cryptotracker/core/presentation/util/ObserveAsEvents.kt
 */
@Composable
fun ObserveSideEffects(effect: Flow<ViewSideEffect>, onAction: (ViewSideEffect) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                effect.collect(onAction)
            }
        }
    }
}