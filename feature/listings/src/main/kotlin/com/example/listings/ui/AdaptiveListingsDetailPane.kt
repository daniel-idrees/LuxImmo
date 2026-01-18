@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.example.listings.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.core.ui.R
import com.example.designsystem.icon.LuxImmoIcon
import com.example.designsystem.icon.LuxImmoIcons
import com.example.designsystem.util.SPACING_MEDIUM
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
    val listingViewModel: ListingViewModel = hiltViewModel()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navigator = rememberListDetailPaneScaffoldNavigator<Int>()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange

    val isDetailVisible by remember(navigator.currentDestination) {
        derivedStateOf {
            navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail
        }
    }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    // for tablets and foldable devices
    val isDualPane by remember(navigator.scaffoldValue) {
        derivedStateOf {
            navigator.scaffoldValue.secondary == PaneAdaptedValue.Expanded &&
                    navigator.scaffoldValue.primary == PaneAdaptedValue.Expanded
        }
    }

    fun navigateToDetail(listingId: Int) {
        scope.launch {
            navigator.navigateTo(
                pane = ListDetailPaneScaffoldRole.Detail,
                contentKey = listingId
            )
        }
    }

    ObserveSideEffects(effect = listingViewModel.effect) { effect ->
        when (effect) {
            is ListingUiEffect.NavigateToDetail -> {
                navigateToDetail(effect.listingId)
            }

            is ListingUiEffect.ShowSnackbar -> showSnackbar(effect.message)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        NavigableListDetailPaneScaffold(
            modifier = modifier,
            navigator = navigator,
            listPane = {
                AnimatedPane {
                    ListingScreen(
                        listingViewModel = listingViewModel,
                        isDetailVisible = isDetailVisible,
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val selectedId = navigator.currentDestination?.contentKey.takeIf {
                        it is Int
                    }
                    if (selectedId == null) {

                        if (isDualPane) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = stringResource(R.string.detail_no_selection),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else if (navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
                            val text = stringResource(R.string.error_unknown)
                            LaunchedEffect(Unit) {
                                //Log the error that selected id is null in the contentKey when detail pane is active
                                scope.launch {
                                    navigator.navigateBack(backNavigationBehavior)
                                }
                                showSnackbar(text)
                            }
                        }
                    } else {

                        val detailViewModel: DetailViewModel =
                            hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                                key = selectedId.toString(),
                            ) { factory ->
                                factory.create(selectedId)
                            }

                        ObserveSideEffects(detailViewModel.errorEffect) { error ->
                            scope.launch {
                                navigator.navigateBack(backNavigationBehavior)
                            }
                            showSnackbar(error)
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded) {
                                IconButton(
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(SPACING_MEDIUM.dp),
                                    onClick = {
                                        scope.launch {
                                            navigator.navigateBack(backNavigationBehavior)
                                        }
                                    }
                                ) {
                                    LuxImmoIcon(LuxImmoIcons.Close)
                                }
                            }

                            ListingDetailScreen(
                                viewModel = detailViewModel
                            )
                        }
                    }
                }
            }
        )
    }
}
