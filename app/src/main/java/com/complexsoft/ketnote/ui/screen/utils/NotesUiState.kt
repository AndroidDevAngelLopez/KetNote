package com.complexsoft.ketnote.ui.screen.utils

import com.complexsoft.ketnote.data.model.Note
import kotlinx.coroutines.flow.Flow

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(val data: List<Note>) : NotesUiState
    data class Error(val error: Throwable) : NotesUiState
}

data class NoteUiState(
    val title: String = "", val text: String = "", val image: String = ""
)
