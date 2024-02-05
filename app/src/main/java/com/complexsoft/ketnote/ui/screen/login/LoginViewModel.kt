package com.complexsoft.ketnote.ui.screen.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.GetActivityForResultUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.StartLoginWithGoogleUseCase
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.AUTH
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.SIGNINREQUEST
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.TAG
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
        fragment: Fragment, activityForResult: ActivityResultLauncher<IntentSenderRequest>
    ) = startLoginWithGoogleUseCase(
        fragment,
        fragment.let { Identity.getSignInClient(it.requireActivity()) },
        SIGNINREQUEST,
        activityForResult,
        TAG
    )

    fun getActivityForResult(fragment: Fragment): ActivityResultLauncher<IntentSenderRequest> =
        getActivityForResultUseCase(
            oneTapClient = fragment.let { Identity.getSignInClient(it.requireActivity()) },
            AUTH,
            fragment,
            TAG
        )
}