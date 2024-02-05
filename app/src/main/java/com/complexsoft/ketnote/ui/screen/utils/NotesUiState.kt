package com.complexsoft.ketnote.ui.screen.utils

import android.net.Uri
import com.complexsoft.ketnote.data.model.Note

sealed interface NotesUiState {
    data object Loading : NotesUiState
    data class Success(val data: List<Note>) : NotesUiState
    data class Error(val error: Throwable) : NotesUiState
}

data class NoteUiState(
    val title: String = "", val text: String = "", val image: Uri = Uri.EMPTY
)

data class NoteJobUiState(
    val value: Boolean = false
)