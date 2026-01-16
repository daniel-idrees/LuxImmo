package com.example.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey
    val id: Int,
    val price: Double,
    val area: Double,
    @ColumnInfo(name = "property_type")
    val propertyType: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    val bedrooms: Int?,
    val rooms: Int?,
    val city: String,
    val professional: String,
    val offerType: Int,
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)

