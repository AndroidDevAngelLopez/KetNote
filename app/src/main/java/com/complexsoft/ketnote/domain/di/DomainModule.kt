package com.complexsoft.ketnote.domain.di

import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.domain.usecases.LoginUseCase
import com.complexsoft.ketnote.domain.usecases.LogoutUseCase
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
    fun providesLoginUseCase(): LoginUseCase = LoginUseCase()

    @Singleton
    @Provides
    fun providesLogoutUseCase(): LogoutUseCase = LogoutUseCase()

    @Singleton
    @Provides
    fun providesHandleNotesUseCase(): HandleNotesUseCase = HandleNotesUseCase()

}