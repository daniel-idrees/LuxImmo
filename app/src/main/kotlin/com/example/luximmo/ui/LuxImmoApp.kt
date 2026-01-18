package com.example.luximmo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    val snackbarHostState = remember { SnackbarHostState() }
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        val notConnectedMessage = stringResource(R.string.error_no_internet)

        LaunchedEffect(isOffline) {
            if (isOffline) {
                snackbarHostState.showSnackbar(
                    message = notConnectedMessage,
                    duration = Indefinite,
                )
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                Box(modifier = Modifier.fillMaxSize()) {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.TopCenter) // Yahan se position change hogi
                    )
                }
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
}
