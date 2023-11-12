package com.complexsoft.ketnote.ui.screen.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.network.firebase.OneTapConstants
import com.complexsoft.ketnote.domain.usecases.GetActivityForResultUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.StartLoginWithGoogleUseCase
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val startLoginWithGoogleUseCase: StartLoginWithGoogleUseCase,
    private val getActivityForResultUseCase: GetActivityForResultUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    val newConnectivityObserver = connectivityUseCase().stateIn(
        scope = viewModelScope,
        initialValue = ConnectivityObserver.Status.Unavailable,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun startLoggingWithGoogle(
        activity: LoginScreen, activityForResult: ActivityResultLauncher<IntentSenderRequest>
    ) = startLoginWithGoogleUseCase(
        activity,
        activity.let { Identity.getSignInClient(it.requireActivity()) },
        OneTapConstants.signInRequest,
        activityForResult,
        OneTapConstants.TAG
    )

    fun getActivityForResult(activity: LoginScreen): ActivityResultLauncher<IntentSenderRequest> =
        getActivityForResultUseCase(
            oneTapClient = activity.let { Identity.getSignInClient(it.requireActivity()) },
            OneTapConstants.auth,
            activity,
            OneTapConstants.TAG
        )
}