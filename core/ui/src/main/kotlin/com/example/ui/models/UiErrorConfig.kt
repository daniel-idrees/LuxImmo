package com.example.ui.models

/**
 * Model to save the error config with error text and optional retry callback
 */
data class UiErrorConfig (
    val errorText: String,
    val onRetry: (() -> Unit)? = null,
)