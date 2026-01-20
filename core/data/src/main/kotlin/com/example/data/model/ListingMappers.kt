package com.example.data.model

import com.example.database.model.ListingEntity
import com.example.domain.model.Listing
import com.example.domain.model.PropertyType
import com.example.network.model.NetworkListing

/**
 * Listing entity converter from Network model to Database model
 */
internal fun NetworkListing.asEntity() = ListingEntity(
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

/**
 * Listing model converter from Database model to Domain model
 */
internal fun ListingEntity.asExternalModel() = Listing(
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
