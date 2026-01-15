package com.example.listings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.ui.navigation.Navigator

fun EntryProviderScope<NavKey>.listingEntry(navigator: Navigator) {
    entry<ListingNavKey> {
        ListingScreen() //TODO add navigation to detail
    }
}