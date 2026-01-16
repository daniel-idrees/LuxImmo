package com.example.luximmo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
fun LuxImmoApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

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