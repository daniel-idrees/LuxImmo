package com.example.detail

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.ui.NavDisplay
import com.example.ui.navigation.Navigator


fun EntryProviderScope<NavKey>.detailEntry(navigator: Navigator) {
    entry<DetailNavKey>(
        metadata = NavDisplay.transitionSpec {
            // Slide new content up, keeping the old content in place underneath
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(1000)
            ) togetherWith ExitTransition.KeepUntilTransitionsFinished
        } + NavDisplay.popTransitionSpec {
            // Slide old content down, revealing the new content in place underneath
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(1000)
                    )
        } + NavDisplay.predictivePopTransitionSpec {
            // Slide old content down, revealing the new content in place underneath
            EnterTransition.None togetherWith
                    slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(1000)
                    )
        }
    ) { key ->
        val id = key.id
        DetailScreen(
            //  showBackButton = true,
            //  onBackClick = { navigator.goBack() },

            viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                key = id,
            ) { factory ->
                factory.create(id)
            },
        )
    }
}

