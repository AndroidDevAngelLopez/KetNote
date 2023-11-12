package com.complexsoft.ketnote.data.di

import android.content.Context
import androidx.room.Room
import com.complexsoft.ketnote.data.local.db.ImagesDatabase
import com.complexsoft.ketnote.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideImagesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, ImagesDatabase::class.java, Constants.IMAGES_DATABASE).build()

    @Singleton
    @Provides
    fun provideImagesToUploadDao(imagesDatabase: ImagesDatabase) = imagesDatabase.imageToUploadDao()

    @Singleton
    @Provides
    fun provideImagesToDeleteDao(imagesDatabase: ImagesDatabase) = imagesDatabase.imageToDeleteDao()
}