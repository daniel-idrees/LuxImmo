package com.example.data.testdoubles

import com.example.database.dao.ListingDao
import com.example.database.model.ListingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestListingDao : ListingDao {
    val entities = mutableMapOf<Int, ListingEntity>()
    private val _flow = MutableStateFlow<List<ListingEntity>>(emptyList())

    override fun getAllListings(): Flow<List<ListingEntity>> = _flow

    override fun getListingById(id: Int): Flow<ListingEntity?> =
        _flow.map { list -> list.find { it.id == id } }

    override suspend fun insertListings(listings: List<ListingEntity>) {
        listings.forEach { entities[it.id] = it }
        _flow.value = entities.values.toList()
    }

    override suspend fun deleteListing(listingId: Int) {
        entities.remove(listingId)
        _flow.value = entities.values.toList()
    }

    override suspend fun upsertListing(listing: ListingEntity) {
        entities[listing.id] = listing
        _flow.value = entities.values.toList()
    }

    override suspend fun deleteAll() {
        entities.clear()
        _flow.value = emptyList()
    }

    override suspend fun replaceListings(listings: List<ListingEntity>) {
        deleteAll()
        insertListings(listings)
    }
}