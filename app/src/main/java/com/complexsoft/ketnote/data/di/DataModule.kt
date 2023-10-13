package com.complexsoft.ketnote.data.di

import android.content.Context
import androidx.room.Room
import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSource
import com.complexsoft.ketnote.data.local.datasource.LocalImagesDataSourceImpl
import com.complexsoft.ketnote.data.local.db.ImagesDatabase
import com.complexsoft.ketnote.data.local.entity.ImageToUploadDAO
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.network.connectivity.NetworkConnectivityObserver
import com.complexsoft.ketnote.data.repository.LocalImagesRepository
import com.complexsoft.ketnote.data.repository.LocalImagesRepositoryImpl
import com.complexsoft.ketnote.utils.Constants.IMAGES_DATABASE
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Singleton
    @Provides
    fun provideLocalImagesRepository(
        localImagesDataSource: LocalImagesDataSource
    ): LocalImagesRepository = LocalImagesRepositoryImpl(localImagesDataSource)

    @Singleton
    @Provides
    fun provideLocalImagesDataSource(
        imagesToUploadDAO: ImageToUploadDAO
    ): LocalImagesDataSource = LocalImagesDataSourceImpl(imagesToUploadDAO)

    @Singleton
    @Provides
    fun provideImagesToUploadDao(imagesDatabase: ImagesDatabase) = imagesDatabase.imageToUploadDao()

    @Singleton
    @Provides
    fun provideImagesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ImagesDatabase::class.java, IMAGES_DATABASE).build()

    @Singleton
    @Provides
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }
}
