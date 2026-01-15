package com.example.data.model

import com.example.database.model.ListingEntity
import com.example.domain.model.Listing
import com.example.domain.model.PropertyType
import com.example.network.model.NetworkListing
import java.text.NumberFormat
import java.util.Locale

fun NetworkListing.asEntity() = ListingEntity(
    id = id,
    price = price,
    area = area,
    propertyType = propertyType,
    imageUrl = url,
    bedrooms = bedrooms,
    rooms = rooms,
    city = city,
    professional = professional,
)

fun ListingEntity.asExternalModel() = Listing(
    id = id,
    price = formatPrice(price),
    area = area,
    city = city,
    rooms = rooms ?: 0 ,
    bedrooms = bedrooms ?: 0 ,
    imageUrl = imageUrl,
    propertyType = PropertyType.fromString(propertyType),
    vendor = professional
)

/**
 *  Formats the given price to France locale
 *  Example: 1500000 -> "1 500 000 €"
 */
private fun formatPrice(price: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.FRANCE).apply {
        maximumFractionDigits = 0
    }.format(price)
}