package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.database.dao.ListingDao
import com.example.database.model.ListingEntity

@Database(entities = [ListingEntity::class], version = 1, exportSchema = false)
abstract class LuxImmoDatabase : RoomDatabase() {
    abstract fun listingDao(): ListingDao
}