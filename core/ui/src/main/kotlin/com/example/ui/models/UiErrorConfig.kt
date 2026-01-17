package com.example.ui.models

data class UiErrorConfig (
    val errorText: String,
    val onRetry: () -> Unit,
)