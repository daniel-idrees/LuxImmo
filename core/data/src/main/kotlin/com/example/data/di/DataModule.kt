package com.example.data.di

import com.example.data.repository.OfflineFirstListingRepository
import com.example.domain.repository.ListingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    internal abstract fun bindsListingRepository(
        listingRepository: OfflineFirstListingRepository,
    ): ListingRepository
}