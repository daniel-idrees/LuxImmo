package com.example.listings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.listings.ui.AdaptiveListingsDetailPane
import com.example.ui.navigation.Navigator

fun EntryProviderScope<NavKey>.listingEntry(navigator: Navigator) {
    entry<ListingNavKey> {
        AdaptiveListingsDetailPane()
    }
}