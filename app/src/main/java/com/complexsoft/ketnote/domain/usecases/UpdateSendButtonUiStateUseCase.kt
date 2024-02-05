package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.ui.screen.utils.SendButtonUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UpdateSendButtonUiStateUseCase {
    val sendButtonUiState = MutableStateFlow(SendButtonUiState())
    operator fun invoke(value: Boolean) = sendButtonUiState.update {
        it.copy(isClicked = value)
    }
}