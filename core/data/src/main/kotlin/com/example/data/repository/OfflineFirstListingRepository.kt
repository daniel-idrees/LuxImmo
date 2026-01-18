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
import com.example.network.toErrorResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineFirstListingRepository @Inject constructor(
    private val gslNetworkDataSource: GslNetworkDataSource,
    private val dao: ListingDao,
) : ListingRepository {

    override val listings: Flow<List<Listing>> =
        dao.getAllListings()
            .map { entities ->
                entities.map(ListingEntity::asExternalModel)
            }

    override fun getListing(listingId: Int): Flow<Listing?> =
        dao.getListingById(listingId)
            .map { it?.asExternalModel() }

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

    override suspend fun refreshListing(listingId: Int): Result<Listing?> {
        runSuspendCatching {
            gslNetworkDataSource.getListing(listingId)
        }.fold(
            onSuccess = { networkListing ->
                val entity = networkListing?.asEntity()
                //null means nothing was found or listing is deleted on remote
                if(entity == null ) {
                    dao.deleteListing(listingId) // delete the cached listing
                } else  {
                    dao.upsertListing(entity)
                }

                return Result.Success(entity?.asExternalModel())
            },
            onFailure = {
                return it.toErrorResult()
            }
        )
    }
}
