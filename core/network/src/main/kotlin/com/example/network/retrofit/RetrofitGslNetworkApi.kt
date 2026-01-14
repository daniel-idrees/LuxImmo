package com.example.network.retrofit

import com.example.network.GslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingResponse
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Retrofit API declaration for Gsl Network API
 */
internal interface RetrofitGslNetworkApi {
    @GET("/listings.json")
    suspend fun getListings(): NetworkListingResponse

    @GET("listings/{listingId}.json")
    suspend fun getListing(
        @Path("listingId") id: Int
    ): NetworkListing
}

/**
 * [Retrofit] backed [GslNetworkDataSource]
 */
@Singleton
internal class RetrofitGslApiClient @Inject constructor(
    private val networkApi: RetrofitGslNetworkApi
) : GslNetworkDataSource {

    override suspend fun getListings(): NetworkListingResponse =
        networkApi.getListings()

    override suspend fun getListing(id: Int): NetworkListing =
        networkApi.getListing(id = id)
}