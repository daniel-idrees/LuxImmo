# Example — Hilt modules for the "Articles" app

Generic, filled-in Hilt modules. Qualifier annotations (`@IoDispatcher`, …) come from this skill's `template.md`.

## Dispatcher + scope providers (`:core:common`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides @IoDispatcher
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides @MainDispatcher
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides @Singleton @ApplicationScope
    fun providesApplicationScope(
        @DefaultDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)
}
```

## Binding an interface to its implementation (`:core:data`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsArticleRepository(
        impl: OfflineFirstArticleRepository,
    ): ArticleRepository
}
```

## Constructing a third-party type (`:core:network`)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun providesJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides @Singleton
    fun providesRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
```
