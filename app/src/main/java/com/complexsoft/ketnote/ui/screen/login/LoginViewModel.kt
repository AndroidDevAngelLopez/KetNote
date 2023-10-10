package com.complexsoft.ketnote.ui.screen.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import com.complexsoft.ketnote.domain.usecases.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    fun startLoggingWithGoogle(
        activity: LoginScreen, activityForResult: ActivityResultLauncher<IntentSenderRequest>
    ) = loginUseCase.startLoggingWithGoogle(activity, activityForResult)


    fun getActivityForResult(activity: LoginScreen): ActivityResultLauncher<IntentSenderRequest> =
        loginUseCase.getActivityForResult(activity)

}