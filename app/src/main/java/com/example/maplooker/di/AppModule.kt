package com.example.maplooker.di

import com.example.maplooker.utils.SchedulerProvider
import com.example.maplooker.utils.SchedulersImpl
import com.example.maplooker.data.LocationCache
import com.example.maplooker.data.RemoteDataSource
import com.example.maplooker.data.Repository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(remoteDataSource: RemoteDataSource, localDataSource: LocationCache): Repository {
        return Repository(remoteDataSource, localDataSource)
    }

    @Singleton
    @Provides
    fun provideRemoteDataSource(okHttpClient: OkHttpClient, gson: Gson): RemoteDataSource {
        return RemoteDataSource(okHttpClient, provideSchedulerProvider(), gson)
    }


    @Singleton
    @Provides
    fun provideSchedulerProvider(): SchedulerProvider {
        return SchedulersImpl()
    }


    @Provides
    @Singleton
    fun provideLocalCache(): LocationCache {
        return LocationCache()
    }
}