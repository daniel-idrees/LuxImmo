package com.example.network.model

import com.example.domain.model.Listing
import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class NetworkListingResponse(
    val items: List<NetworkListing>,
    val totalCount: Int
)
