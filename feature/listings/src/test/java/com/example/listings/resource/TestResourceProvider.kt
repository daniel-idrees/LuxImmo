package com.example.listings.resource

import com.example.core.ui.R
import com.example.ui.resource.ResourceProvider

class TestResourceProvider : ResourceProvider {
    override fun getString(resId: Int, vararg args: Any): String {
        return when (resId) {
            R.string.area_format -> "${args[0]} m²"
            R.string.price_per_square_meter_format -> "${args[0]}/m²"
            R.string.error_not_found -> "Listing not found"
            R.string.error_offline -> "Offline"
            R.string.error_unknown -> "An unknown error occurred"
            R.string.error_no_internet -> "No Internet"
            else -> ""
        }
    }

    override fun getQuantityString(resId: Int, quantity: Int, vararg args: Any): String {
        return when (resId) {
            R.plurals.listing_rooms_format -> "$quantity rooms"
            R.plurals.listing_bedrooms_format -> "$quantity bedrooms"
            else -> ""
        }
    }
}
