package com.complexsoft.ketnote.ui.screen

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import com.complexsoft.ketnote.ui.screen.login.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")
val isOnBoardingCompleted = booleanPreferencesKey("isCompleted")
val isUserLogged = booleanPreferencesKey("isLogged")
val getUserDataToken = stringPreferencesKey("getUserDataToken")
val getUserDataUserName = stringPreferencesKey("getUserDataUserName")
val getUserDataProfilePic = stringPreferencesKey("getUserDataProfilePic")

class MainViewModel : ViewModel() {

    private val _onBoardingCompletedValue = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onBoardingCompletedValue


    val userToSave = User("", "", "")

    private val _userLoggedInComplete = MutableStateFlow(false)
    val userLoggedInComplete: StateFlow<Boolean> = _userLoggedInComplete

    fun userLoggedCompleted(user: User) {
        _userLoggedInComplete.value = true
        userToSave.idToken = user.idToken
        userToSave.username = user.username
        userToSave.userImage = user.userImage

    }

    fun onBoardingComplete() {
        _onBoardingCompletedValue.value = true
    }
}