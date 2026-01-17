package com.example.data.repository

import com.example.data.model.asEntity
import com.example.data.model.asExternalModel
import com.example.database.dao.ListingDao
import com.example.database.model.ListingEntity
import com.example.domain.Result
import com.example.domain.exception.ListingDetailUnavailableException
import com.example.domain.model.Listing
import com.example.domain.repository.ListingRepository
import com.example.network.GslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.runSuspendCatching
import com.example.network.toErrorResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

internal class OfflineFirstListingRepository @Inject constructor(
    private val gslNetworkDataSource: GslNetworkDataSource,
    private val dao: ListingDao,
) : ListingRepository {

    /**
     *
     */
    override val listingsRefreshStatus: Flow<Result>
        field = MutableSharedFlow<Result>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    override fun observeListings(): Flow<List<Listing>> = dao.getAllListings()
        .onStart {
            refreshListings()
        }
        .map { entities ->
            entities.map(ListingEntity::asExternalModel)
        }

    override fun getListing(listingId: Int): Flow<Listing> = dao.getListingById(listingId)
        .transform { entity ->
            if (entity == null) {
                val result = refreshListingDetails(listingId)
                if (result is Result.Error) {
                    throw ListingDetailUnavailableException(result)
                }
            } else {
                emit(entity)
            }
        }
        .map { it.asExternalModel() }

    override suspend fun refreshListings() {
        runSuspendCatching {
            gslNetworkDataSource.getListings()
        }.fold(
            onSuccess = { networkListings ->
                dao.replaceListings(
                    networkListings.items.map(NetworkListing::asEntity)
                )
                listingsRefreshStatus.emit(Result.Success)
            },
            onFailure = {
                val errorResult = it.toErrorResult()
                listingsRefreshStatus.emit(errorResult)
            }
        )
    }

    private suspend fun refreshListingDetails(listingId: Int): Result {
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
