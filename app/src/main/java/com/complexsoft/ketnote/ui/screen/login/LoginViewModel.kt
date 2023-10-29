package com.complexsoft.ketnote.ui.screen.login

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    private val _connectivityStateFlow: MutableStateFlow<ConnectivityObserver.Status> =
        MutableStateFlow(ConnectivityObserver.Status.Unavailable)
    val connectivityStateFlow: StateFlow<ConnectivityObserver.Status> = _connectivityStateFlow

    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        connectivityUseCase().onEach {
            when (it) {
                ConnectivityObserver.Status.Unavailable -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Unavailable
                }

                ConnectivityObserver.Status.Losing -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Losing
                }

                ConnectivityObserver.Status.Available -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Available
                }

                ConnectivityObserver.Status.Lost -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Lost
                }
            }

        }.launchIn(viewModelScope)
    }

    fun startLoggingWithGoogle(
        activity: LoginScreen, activityForResult: ActivityResultLauncher<IntentSenderRequest>
    ) = loginUseCase.startLoggingWithGoogle(activity, activityForResult)


    fun getActivityForResult(activity: LoginScreen): ActivityResultLauncher<IntentSenderRequest> =
        loginUseCase.getActivityForResult(activity)

}