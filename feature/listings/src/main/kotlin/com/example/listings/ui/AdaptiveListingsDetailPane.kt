@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.example.listings.ui

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.listings.ui.detail.DetailViewModel
import com.example.listings.ui.detail.ListingDetailScreen
import com.example.listings.ui.list.ListingScreen
import com.example.listings.ui.list.ListingUiEffect
import com.example.listings.ui.list.ListingViewModel
import com.example.ui.util.ObserveSideEffects
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun AdaptiveListingsDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel : ListingViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()

    val isDetailVisible by remember (navigator.currentDestination) {
        derivedStateOf {
            navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail
        }
    }

    ObserveSideEffects(effect = viewModel.effect) { effect ->
        when (effect) {
            is ListingUiEffect.NavigateToDetail -> {
                scope.launch {
                    navigator.navigateTo(
                        pane = ListDetailPaneScaffoldRole.Detail,
                        contentKey = effect.id
                    )
                }
            }

            is ListingUiEffect.ShowSnackbar -> {
                scope.launch {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        NavigableListDetailPaneScaffold(
            modifier = modifier,
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    ListingScreen(
                        listingViewModel = viewModel,
                        isDetailVisible = isDetailVisible,
                    )
                }
            },
            detailPane = {
                val selectedId = navigator.currentDestination?.contentKey
                if (selectedId != null) {
                    AnimatedPane {
                        ListingDetailScreen(
                            viewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                                key = selectedId,
                            ) { factory ->
                                factory.create(selectedId)
                            },
                        )
                    }
                }
            }
        )
    }
}
