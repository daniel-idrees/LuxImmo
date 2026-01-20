package com.example.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.database.model.ListingEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for [ListingEntity] access
 */
@Dao
interface ListingDao {
    @Query("SELECT * FROM listings")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getListingById(id: Int): Flow<ListingEntity?>

    /**
     * Inserts or updates [entities] in the db under the specified primary keys
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Query("DELETE FROM listings WHERE id = :listingId")
    suspend fun deleteListing(listingId: Int)

    @Query("DELETE FROM listings")
    suspend fun deleteAll()

    /**
     * Deletes existing data and inserts new data in a single transaction.
     * This ensures the UI doesn't see an empty screen during the transition.
     */
    @Transaction
    suspend fun replaceListings(listings: List<ListingEntity>) {
        deleteAll()
        insertListings(listings)
    }
}