package com.example.network

import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingResponse

/**
 * Interface representing network calls to the Gsl backend
 */
interface GslNetworkDataSource {
    suspend fun getListings(): NetworkListingResponse
    suspend fun getListing(id: Int): NetworkListing
}