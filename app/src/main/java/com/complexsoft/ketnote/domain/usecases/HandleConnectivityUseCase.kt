package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import javax.inject.Inject

class HandleConnectivityUseCase @Inject constructor(
    private val networkConnectivityObserver: ConnectivityObserver
) {
    operator fun invoke() = networkConnectivityObserver.observe()
}