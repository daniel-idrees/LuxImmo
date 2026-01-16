package com.example.listings.models

import com.example.core.ui.R
import com.example.domain.model.Listing
import com.example.ui.helper.toCurrency
import com.example.ui.models.DisplayDoubleValue
import com.example.ui.models.DisplayIntValue
import com.example.ui.resource.ResourceProvider
import java.util.Locale

internal data class ListingUi(
    val id: Int,
    val price: DisplayDoubleValue,
    val pricePerSquareMeter: DisplayDoubleValue,
    val rooms: DisplayIntValue?,
    val bedrooms: DisplayIntValue?,
    val city: String,
    val area: DisplayDoubleValue,
    val imageUrl: String?,
    val vendor: String,
    val propertyType: String,
)

internal fun Listing.toListingUi(resourceProvider: ResourceProvider): ListingUi =
    ListingUi(
        id = id,
        price = price.toFrenchCurrency(),
        pricePerSquareMeter = pricePerSquareMeter(resourceProvider),
        rooms = toDisplayValueRooms(resourceProvider),
        bedrooms = toDisplayValueBedrooms(resourceProvider),
        city = city,
        area = toDisplayValueArea(resourceProvider),
        imageUrl = imageUrl,
        vendor = vendor,
        propertyType = propertyType.value,
    )


private fun Listing.toDisplayValueRooms(resourceProvider: ResourceProvider): DisplayIntValue? =
    rooms?.let { rooms ->
        DisplayIntValue(
            value = rooms,
            formatted = resourceProvider.getQuantityString(
                R.plurals.listing_rooms_format,
                rooms,
                rooms
            )
        )
    }

private fun Listing.toDisplayValueBedrooms(resourceProvider: ResourceProvider): DisplayIntValue? =
    bedrooms?.let { bedrooms ->
        DisplayIntValue(
            value = bedrooms,
            formatted = resourceProvider.getQuantityString(
                R.plurals.listing_bedrooms_format,
                bedrooms,
                bedrooms
            )
        )
    }


private fun Listing.toDisplayValueArea(resourceProvider: ResourceProvider): DisplayDoubleValue =
    DisplayDoubleValue(
        value = area,
        formatted = resourceProvider.getString(R.string.area_format, area)
    )

private fun Double.toFrenchCurrency(): DisplayDoubleValue =
    DisplayDoubleValue(
        value = this,
        formatted = this.toCurrency(locale = Locale.FRANCE)
    )

private fun Listing.pricePerSquareMeter(resourceProvider: ResourceProvider): DisplayDoubleValue {
    val pricePerSquareMeter = price / area
    val formattedPrice = pricePerSquareMeter.toCurrency(locale = Locale.FRANCE)
    return DisplayDoubleValue(
        value = pricePerSquareMeter,
        formatted = resourceProvider.getString(R.string.price_per_square_meter_format, formattedPrice)
    )
}


