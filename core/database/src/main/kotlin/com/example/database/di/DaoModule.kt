package com.example.database.di

import com.example.database.LuxImmoDatabase
import com.example.database.dao.ListingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Provides
    fun providesListingDao(
        database: LuxImmoDatabase
    ): ListingDao = database.listingDao()
}