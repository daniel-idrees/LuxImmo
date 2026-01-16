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
    offerType = offerType
)

fun ListingEntity.asExternalModel() = Listing(
    id = id,
    price = price,
    area = area,
    city = city,
    rooms = rooms,
    bedrooms = bedrooms,
    imageUrl = imageUrl,
    propertyType = PropertyType.fromString(propertyType),
    vendor = professional,
    offerType = offerType
)
