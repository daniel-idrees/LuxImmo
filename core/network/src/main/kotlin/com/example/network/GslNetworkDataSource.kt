package com.example.network

import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingsResponse
import retrofit2.Response

/**
 * Interface representing network calls to the Gsl backend
 */
interface GslNetworkDataSource {
    suspend fun getListings(): NetworkListingsResponse
    suspend fun getListing(id: Int): NetworkListing?
}