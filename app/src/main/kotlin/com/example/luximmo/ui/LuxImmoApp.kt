package com.example.luximmo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.core.ui.R
import com.example.listings.navigation.ListingNavKey
import com.example.listings.navigation.listingEntry
import com.example.ui.navigation.Navigator


@Composable
private fun rememberNavigator(startKey: NavKey): Navigator {
    val backStack = rememberNavBackStack(startKey)
    return remember(startKey) {
        Navigator(backStack)
    }
}

@Composable
fun LuxImmoApp(appState: LuxImmoAppState) {
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            OfflineBottomBar(visible = isOffline)
        }
    ) { innerPadding ->

        val navigator = rememberNavigator(startKey = ListingNavKey)

        val entryProvider = entryProvider {
            listingEntry(navigator)
        }

        NavDisplay(
            backStack = navigator.backStack,
            onBack = { navigator.goBack() },
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider,
        )
    }
}

@Composable
private fun OfflineBottomBar(
    modifier: Modifier = Modifier,
    visible: Boolean
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
    ) {
        BottomAppBar {
            Text(
                text = stringResource(R.string.error_no_internet),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}