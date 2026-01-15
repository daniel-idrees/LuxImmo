package com.example.ui.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class Navigator(
    val backStack: NavBackStack<NavKey>,
) {

    fun navigate(key: NavKey) {
        // "Reorder to top" logic
        if (backStack.contains(key)) {
            backStack.remove(key)
        }
        backStack.add(key)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}