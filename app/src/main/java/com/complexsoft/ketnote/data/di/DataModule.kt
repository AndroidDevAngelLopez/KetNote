package com.complexsoft.ketnote.data.di

import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSource
import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSourceImpl
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.network.connectivity.NetworkConnectivityObserver
import com.complexsoft.ketnote.data.repository.LocalImagesRepository
import com.complexsoft.ketnote.data.repository.LocalImagesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    fun provideLocalImagesRepository(
        localImagesRepositoryImpl: LocalImagesRepositoryImpl
    ): LocalImagesRepository

    @Binds
    fun provideLocalImagesDataSource(
        localImagesDataSourceImpl: LocalImagesDataSourceImpl
    ): LocalImagesDataSource

    @Binds
    fun provideConnectivityObserver(
        networkConnectivityObserver: NetworkConnectivityObserver
    ): ConnectivityObserver
}
