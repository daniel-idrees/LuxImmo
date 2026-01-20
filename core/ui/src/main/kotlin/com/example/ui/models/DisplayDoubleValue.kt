package com.example.ui.models

/**
 * Model to save the actual Double value and the formatted value to be displayed
 */
data class DisplayDoubleValue(
    val value: Double,
    val formatted: String
)

/**
 * Model to save the actual Integer value and the formatted value to be displayed
 */
data class DisplayIntValue(
    val value: Int,
    val formatted: String
)