package com.example.ui.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats a Double into a currency string.
 * Defaults to France/Euro but can be overridden.
 */
fun Double.toCurrency(locale: Locale = Locale.FRANCE): String {
    return NumberFormat.getCurrencyInstance(locale).apply {
        maximumFractionDigits = 0
    }.format(this)
}