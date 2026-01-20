package com.example.network.model

import kotlinx.serialization.Serializable

/**
 * Network representation of a list of  [com.example.domain.model.Listing]
 */
@Serializable
data class NetworkListingsResponse(
    val items: List<NetworkListing>,
    val totalCount: Int
)
