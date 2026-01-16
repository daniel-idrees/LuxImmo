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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException
import javax.inject.Inject


class OfflineFirstListingRepository @Inject constructor(
    private val gslNetworkDataSource: GslNetworkDataSource,
    private val dao: ListingDao,
) : ListingRepository {

    override val refreshListResultEvent : SharedFlow<Result>
        field = MutableSharedFlow<Result>(
            replay = 0,
            extraBufferCapacity = 1
        )

    override val listings: Flow<List<Listing>> = dao.getAllListings()
        .onStart {
            refreshListings()
        }
        .map { entities ->
            entities.map(ListingEntity::asExternalModel)
        }

    override fun getListing(listingId: Int): Flow<Listing?> = dao.getListingById(listingId)
        .onStart {
            val listing = dao.getListingByIdOnce(listingId)
            if (listing == null) {
                refreshListingDetails(listingId)
            }
        }
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
                refreshListResultEvent.emit(Result.Success)
                return Result.Success
            },
            onFailure = {
                val errorResult = it.toErrorResult()
                refreshListResultEvent.emit(errorResult)
                return errorResult
            }
        )
    }
}

private fun Throwable.toErrorResult(): Result =
    when (this) {
        is IOException -> Result.Error.NoInternetConnection
        else -> Result.Error.Unknown
    }
