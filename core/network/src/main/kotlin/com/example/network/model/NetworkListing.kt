package com.example.network.model

import kotlinx.serialization.Serializable


/**
 * Network representation of a [com.example.domain.model.Listing]
 */
@Serializable
data class NetworkListing (
    val bedrooms: Int? = null,
    val city: String,
    val id: Int,
    val area: Double,
    val url: String? = null,
    val price: Double,
    val professional: String,
    val propertyType: String,
    val offerType: Int,
    val rooms: Int? = null,
)

