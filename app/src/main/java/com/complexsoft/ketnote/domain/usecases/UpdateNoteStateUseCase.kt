package com.complexsoft.ketnote.domain.usecases

import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UpdateNoteStateUseCase {
    val noteUiState = MutableStateFlow(NoteUiState())
    operator fun invoke(
        title: String, text: String, imageUri: String
    ) = noteUiState.update {
        it.copy(
            title = title, text = text, image = imageUri
        )
    }

}