package com.complexsoft.ketnote.ui.screen

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")
val isOnBoardingCompleted = booleanPreferencesKey("isCompleted")

class MainViewModel(

) : ViewModel() {

    private val _onBoardingCompletedValue = MutableStateFlow(false)
    val onboardingCompleted : StateFlow<Boolean> = _onBoardingCompletedValue

    fun onBoardingComplete(){
      _onBoardingCompletedValue.value = true
    }
}