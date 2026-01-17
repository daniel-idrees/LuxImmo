package com.example.data.repository

import com.example.data.model.asEntity
import com.example.data.model.asExternalModel
import com.example.database.dao.ListingDao
import com.example.database.model.ListingEntity
import com.example.domain.Result
import com.example.domain.exception.ListingDetailUnavailableException
import com.example.domain.exception.ListingsUnavailableException
import com.example.domain.model.Listing
import com.example.domain.repository.ListingRepository
import com.example.network.GslNetworkDataSource
import com.example.network.model.NetworkListing
import com.example.network.runSuspendCatching
import com.example.network.toErrorResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

internal class OfflineFirstListingRepository @Inject constructor(
    private val gslNetworkDataSource: GslNetworkDataSource,
    private val dao: ListingDao,
) : ListingRepository {

    override fun observeListings(): Flow<List<Listing>> = dao.getAllListings()
        .onStart {
            val result = refreshListings()
            if (result is Result.Error) {
                throw ListingsUnavailableException(result)
            }
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

    override suspend fun refreshListings(): Result<List<Listing>> {
        runSuspendCatching {
            gslNetworkDataSource.getListings()
        }.fold(
            onSuccess = { networkListings ->
                val entities = networkListings.items.map(NetworkListing::asEntity)
                dao.replaceListings(entities)

                val listing = entities.map(ListingEntity::asExternalModel)
                return Result.Success(listing)
            },
            onFailure = {
                val errorResult = it.toErrorResult()
                return errorResult
            }
        )
    }

    private suspend fun refreshListingDetails(listingId: Int): Result<ListingEntity> {
        runSuspendCatching {
            gslNetworkDataSource.getListing(listingId)
        }.fold(
            onSuccess = { networkListing ->
                val entity = networkListing.asEntity()
                dao.upsertListing(entity)
                return Result.Success(entity)
            },
            onFailure = {
                return it.toErrorResult()
            }
        )
    }
}
