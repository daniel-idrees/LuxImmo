package com.example.network.di

import android.content.Context
import com.example.luximmo.core.network.BuildConfig
import com.example.network.GslNetworkDataSource
import com.example.network.demo.DemoAssetManager
import com.example.network.demo.DemoGslNetworkDataSource
import com.example.network.retrofit.RetrofitGslApiClient
import com.example.network.retrofit.RetrofitGslNetworkApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
internal object NetworkModule {

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
    fun providesDemoAssetManager(
        @ApplicationContext context: Context,
    ): DemoAssetManager = DemoAssetManager(context.assets::open)

    /**
     * Provides the [GslNetworkDataSource] implementation used across the app.
     *
     * When [BuildConfig.BASE_URL] is blank (e.g. no `BASE_URL` is set in `local.properties`),
     * the app falls back to [DemoGslNetworkDataSource], which serves bundled demo data from
     * local JSON assets. Otherwise the Retrofit-backed [RetrofitGslApiClient] is used.
     *
     * Both implementations are wrapped in [dagger.Lazy] so the Retrofit client is never
     * constructed with an empty base URL (which would throw at runtime).
     */
    @Provides
    @Singleton
    fun provideGslNetworkDataSource(
        retrofitClient: dagger.Lazy<RetrofitGslApiClient>,
        demoDataSource: dagger.Lazy<DemoGslNetworkDataSource>,
    ): GslNetworkDataSource =
        if (BuildConfig.BASE_URL.isBlank()) {
            demoDataSource.get()
        } else {
            retrofitClient.get()
        }
}
