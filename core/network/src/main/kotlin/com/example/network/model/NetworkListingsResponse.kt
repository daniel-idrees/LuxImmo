package com.example.network.model

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class NetworkListingsResponse(
    val items: List<NetworkListing>,
    val totalCount: Int
)
