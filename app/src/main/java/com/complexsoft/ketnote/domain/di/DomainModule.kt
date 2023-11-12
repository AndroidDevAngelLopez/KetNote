package com.complexsoft.ketnote.domain.di

import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.DeletePhotoFromFirebaseUseCase
import com.complexsoft.ketnote.domain.usecases.GetActivityForResultUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.LogoutUseCase
import com.complexsoft.ketnote.domain.usecases.StartLoginWithGoogleUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateIsNoteJobDoneUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNoteStateUseCase
import com.complexsoft.ketnote.domain.usecases.UploadPhotoToFirebaseUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Singleton
    @Provides
    fun getActivityForResultUseCase(): GetActivityForResultUseCase = GetActivityForResultUseCase()

    @Singleton
    @Provides
    fun getStartLoginWithGoogleUseCase(): StartLoginWithGoogleUseCase =
        StartLoginWithGoogleUseCase()

    @Singleton
    @Provides
    fun providesLogoutUseCase(): LogoutUseCase = LogoutUseCase()

    @Singleton
    @Provides
    fun providesUpdateIsNoteJobDoneUseCase(): UpdateIsNoteJobDoneUseCase =
        UpdateIsNoteJobDoneUseCase()

    @Singleton
    @Provides
    fun providesUpdateNoteStateUseCase(): UpdateNoteStateUseCase =
        UpdateNoteStateUseCase()

    @Singleton
    @Provides
    fun providesUploadPhotoToFirebaseUseCase(): UploadPhotoToFirebaseUseCase =
        UploadPhotoToFirebaseUseCase()


    @Singleton
    @Provides
    fun providesDeletePhotoFromFirebaseUseCase(): DeletePhotoFromFirebaseUseCase =
        DeletePhotoFromFirebaseUseCase()

    @Singleton
    @Provides
    fun providesHandleConnectivityUseCase(connectivityObserver: ConnectivityObserver): HandleConnectivityUseCase =
        HandleConnectivityUseCase(connectivityObserver)

}