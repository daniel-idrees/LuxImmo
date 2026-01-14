package com.example.network.di

import com.example.luximmo.core.network.data.BuildConfig
import com.example.network.GslNetworkDataSource
import com.example.network.retrofit.RetrofitGslApiClient
import com.example.network.retrofit.RetrofitGslNetworkApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    @Provides
    @Singleton
    fun provideRetrofitBuilder(): Retrofit.Builder = Retrofit.Builder()

    @Provides
    @Singleton
    fun okHttpCallFactory(httpLoggingInterceptor: HttpLoggingInterceptor): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofitGslNetworkApi(
        retrofitBuilder: Retrofit.Builder,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
        networkJson: Json
    ): RetrofitGslNetworkApi =
        retrofitBuilder
            .baseUrl(BuildConfig.BASE_URL)
            .callFactory { okhttpCallFactory.get().newCall(it) }
            .addConverterFactory(
                networkJson.asConverterFactory("application/json".toMediaType()),
            )
            .build()
            .create(RetrofitGslNetworkApi::class.java)

    @Provides
    @Singleton
    fun provideGslNetworkDataSource(
        network: RetrofitGslApiClient
    ): GslNetworkDataSource = network
}
