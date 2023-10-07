package com.complexsoft.ketnote.ui.screen.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboard_login")
val isOnBoardingCompleted = booleanPreferencesKey("isOnBoardingCompleted")
class OnBoardingViewModel : ViewModel() {

    fun onBoardingComplete(context: Context) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                val currentValue = settings[isOnBoardingCompleted] ?: false
                settings[isOnBoardingCompleted] = !currentValue
            }
        }
    }
}