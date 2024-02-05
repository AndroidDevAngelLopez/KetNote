package com.complexsoft.ketnote.ui.screen.utils

import com.complexsoft.ketnote.data.model.NotificationItem

sealed interface NotificationsUiState {
    data object Loading : NotificationsUiState
    data class Success(val data: List<NotificationItem>) : NotificationsUiState
    data class Error(val error: Throwable) : NotificationsUiState
}
