package com.complexsoft.ketnote.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.domain.usecases.UpdateNotificationsButtonUseCase
import com.complexsoft.ketnote.ui.screen.utils.NotificationsButtonState
import com.complexsoft.ketnote.ui.screen.utils.NotificationsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenNotificationsViewModel @Inject constructor(
    private val updateNotificationsButtonUseCase: UpdateNotificationsButtonUseCase,
) : ViewModel() {

    val notificationsButtonState = updateNotificationsButtonUseCase.notificationsUiState.stateIn(
        scope = viewModelScope,
        initialValue = NotificationsButtonState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val notificationsFlow: StateFlow<NotificationsUiState> = MongoDB.getNotifications().map(
        NotificationsUiState::Success
    ).stateIn(
        scope = viewModelScope,
        initialValue = NotificationsUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun updateNotificationsButtonState(value: Boolean) = updateNotificationsButtonUseCase(value)

    fun clearAllNotifications() {
        viewModelScope.launch {
            MongoDB.deleteAllNotifications()
        }
    }

    fun insertNotification(title: String, description: String) {
        viewModelScope.launch {
            MongoDB.createNotification(title, description, false)
        }
    }
}