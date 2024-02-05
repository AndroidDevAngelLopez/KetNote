package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.ui.screen.utils.NotificationsButtonState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UpdateNotificationsButtonUseCase {
    val notificationsUiState = MutableStateFlow(NotificationsButtonState())
    operator fun invoke(value: Boolean) = notificationsUiState.update {
        it.copy(isClicked = value)
    }
}