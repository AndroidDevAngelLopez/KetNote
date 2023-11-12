package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.ui.screen.utils.NoteJobUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UpdateIsNoteJobDoneUseCase {

    val isNoteJobDone = MutableStateFlow(NoteJobUiState())
    operator fun invoke(value: Boolean) = isNoteJobDone.update {
        it.copy(value = value)
    }
}