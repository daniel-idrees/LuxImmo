package com.example.data.repository

import com.example.data.model.asEntity
import com.example.data.model.asExternalModel
import com.example.database.dao.ListingDao
import com.example.database.model.ListingEntity
import com.example.domain.Result
import com.example.domain.model.Listing
import com.example.domain.repository.ListingRepository
import com.example.network.GslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.runSuspendCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


class OfflineFirstListingRepository @Inject constructor(
    private val gslNetworkDataSource: GslNetworkDataSource,
    private val dao: ListingDao,
) : ListingRepository {

    override val listings: Flow<List<Listing>> = dao.getAllListings()
        .map { entities ->
            entities.map(ListingEntity::asExternalModel)
        }

    override suspend fun getListing(listingId: Int): Flow<Listing?> = dao.getListingById(listingId)
        .map { it?.asExternalModel() }

    override suspend fun refreshListings(): Result {
        runSuspendCatching {
            gslNetworkDataSource.getListings()
        }.fold(
            onSuccess = { networkListings ->
                dao.replaceListings(
                    networkListings.items.map(NetworkListing::asEntity)
                )
                return Result.Success
            },
            onFailure = {
                return it.toErrorResult()
            }
        )
    }

    override suspend fun refreshListingDetails(listingId: Int): Result {
        runSuspendCatching {
            gslNetworkDataSource.getListing(listingId)
        }.fold(
            onSuccess = { networkListing ->
                dao.upsertListing(networkListing.asEntity())
                return Result.Success
            },
            onFailure = {
                return it.toErrorResult()
            }
        )
    }
}

private fun Throwable.toErrorResult(): Result =
    when (this) {
        is IOException -> Result.Error.NoInternetConnection
        else -> Result.Error.Unknown
    }
