package com.example.database.di

import android.content.Context
import androidx.room.Room
import com.example.database.LuxImmoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): LuxImmoDatabase = Room.databaseBuilder(
        context,
        LuxImmoDatabase::class.java,
        listingDatabaseName
    ).build()
}

private const val listingDatabaseName = "listing_database"