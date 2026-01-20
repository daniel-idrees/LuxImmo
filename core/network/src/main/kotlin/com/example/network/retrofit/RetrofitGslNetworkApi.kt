package com.example.network.retrofit

import com.example.network.GslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.model.NetworkListingsResponse
import retrofit2.HttpException
import retrofit2.Response
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
    suspend fun getListings(): NetworkListingsResponse

    @GET("listings/{listingId}.json")
    suspend fun getListing(
        @Path("listingId") id: Int
    ): Response<NetworkListing?>
}

/**
 * [Retrofit] backed [GslNetworkDataSource]
 */
@Singleton
internal class RetrofitGslApiClient @Inject constructor(
    private val networkApi: RetrofitGslNetworkApi
) : GslNetworkDataSource {

    override suspend fun getListings(): NetworkListingsResponse =
        networkApi.getListings()

    /**
     * If the network returns code 403 or 404, we assume that listing is not found
     * hence, we return null
     * For other network errors, HttpException is thrown
     */
    override suspend fun getListing(id: Int): NetworkListing? {
        val response = networkApi.getListing(id = id)
        return when {
            response.isSuccessful -> {
                response.body()
            }
            response.code() == 403 || response.code() == 404 -> {
                //either access was forbidden or listing was not found
                null
            }
            else -> {
                throw HttpException(response)
            }
        }
    }
}