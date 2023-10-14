package com.complexsoft.ketnote.ui.screen.utils

sealed class NotesState<out T> {
    data object Idle : NotesState<Nothing>()
    data object Loading : NotesState<Nothing>()
    data class Success<T>(val data: T) : NotesState<T>()
    data class Error(val error: Throwable) : NotesState<Nothing>()
}

data class NoteUiState(
    val title: String = "", val text: String = "", val image: String = ""
)
