package com.complexsoft.ketnote.domain.di

import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.CreateNoteUseCase
import com.complexsoft.ketnote.domain.usecases.DeletePhotoFromFirebaseUseCase
import com.complexsoft.ketnote.domain.usecases.GeminiUseCase
import com.complexsoft.ketnote.domain.usecases.GetActivityForResultUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleLogoutUseCase
import com.complexsoft.ketnote.domain.usecases.OpenImageChooserUseCase
import com.complexsoft.ketnote.domain.usecases.ShareToInstagramUseCase
import com.complexsoft.ketnote.domain.usecases.SpeechToTextUseCase
import com.complexsoft.ketnote.domain.usecases.StartLoginWithGoogleUseCase
import com.complexsoft.ketnote.domain.usecases.TextToSpeechUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateIsNoteJobDoneUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNoteStateUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNotificationsButtonUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateSendButtonUiStateUseCase
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
    fun providesActivityForResultUseCase(): GetActivityForResultUseCase =
        GetActivityForResultUseCase()

    @Singleton
    @Provides
    fun providesStartLoginWithGoogleUseCase(): StartLoginWithGoogleUseCase =
        StartLoginWithGoogleUseCase()

    @Singleton
    @Provides
    fun providesUpdateSendButtonStatusUiState(): UpdateSendButtonUiStateUseCase =
        UpdateSendButtonUiStateUseCase()

    @Singleton
    @Provides
    fun providesLogoutUseCase(): HandleLogoutUseCase = HandleLogoutUseCase()

    @Singleton
    @Provides
    fun providesUpdateIsNoteJobDoneUseCase(): UpdateIsNoteJobDoneUseCase =
        UpdateIsNoteJobDoneUseCase()

    @Singleton
    @Provides
    fun providesUpdateNoteStateUseCase(): UpdateNoteStateUseCase = UpdateNoteStateUseCase()

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
    fun provideUpdateNotificationsButtonUseCase(): UpdateNotificationsButtonUseCase =
        UpdateNotificationsButtonUseCase()

    @Singleton
    @Provides
    fun providesCreateNoteUseCase(uploadPhotoToFirebaseUseCase: UploadPhotoToFirebaseUseCase): CreateNoteUseCase =
        CreateNoteUseCase(uploadPhotoToFirebaseUseCase)

    @Singleton
    @Provides
    fun providesShareWithInstagramUseCase(): ShareToInstagramUseCase = ShareToInstagramUseCase()

    @Singleton
    @Provides
    fun providesOpenImageChooserUseCase(): OpenImageChooserUseCase = OpenImageChooserUseCase()


    @Singleton
    @Provides
    fun provideSpeechToTextUseCase(): SpeechToTextUseCase = SpeechToTextUseCase()

    @Singleton
    @Provides
    fun provideTextToSpeechUseCase(): TextToSpeechUseCase = TextToSpeechUseCase()

    @Singleton
    @Provides
    fun providesGeminiUseCase(): GeminiUseCase = GeminiUseCase()

    @Singleton
    @Provides
    fun providesHandleConnectivityUseCase(connectivityObserver: ConnectivityObserver): HandleConnectivityUseCase =
        HandleConnectivityUseCase(connectivityObserver)

}