package com.example.testing.data

import com.example.domain.model.Listing
import com.example.domain.model.PropertyType


val listingsTestData = listOf(
    Listing(
        id = 1,
        price = 200000.0,
        area = 100.0,
        city = "A",
        propertyType = PropertyType.MAISON_VILLA,
        bedrooms = 3,
        rooms = 5,
        vendor = "V1",
        offerType = 1,
        imageUrl = ""
    ),
    Listing(
        id = 2,
        price = 100000.0,
        area = 50.0,
        city = "B",
        propertyType = PropertyType.MAISON_VILLA,
        bedrooms = 1,
        rooms = 2,
        vendor = "V2",
        offerType = 2,
        imageUrl = ""
    ),
    Listing(
        id = 3,
        price = 300000.0,
        area = 450.0,
        city = "C",
        propertyType = PropertyType.MAISON_VILLA,
        bedrooms = 1,
        rooms = 2,
        vendor = "V2",
        offerType = 2,
        imageUrl = ""
    ),
    Listing(
        id = 4,
        price = 500000.0,
        area = 550.0,
        city = "D",
        propertyType = PropertyType.MAISON_VILLA,
        bedrooms = 1,
        rooms = 2,
        vendor = "V2",
        offerType = 2,
        imageUrl = ""
    )
)

val testListingData = listingsTestData[0]