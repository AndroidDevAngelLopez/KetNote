package com.complexsoft.ketnote.ui.screen.home

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.MongoDB
import com.complexsoft.ketnote.domain.usecases.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _notesFlow = MutableStateFlow(NotesUiState.Success(emptyList()))
    val notesFlow: StateFlow<NotesUiState> = _notesFlow


    private val _searchedNotesFlow = MutableStateFlow(NotesUiState.Success(emptyList()))
    val searchedNotesFlow: StateFlow<NotesUiState> = _searchedNotesFlow

    sealed class NotesUiState {
        data class Success(val notes: List<Note>) : NotesUiState()
        data class Error(val exception: Throwable) : NotesUiState()
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAllNotes()
        }
    }

    fun logout(activity: FragmentActivity) {
        logoutUseCase.logoutUser(activity)
    }

    fun searchNotesByTitle(title: String) {
        viewModelScope.launch {
            MongoDB.searchNotesByTitle(title).collectLatest {
                _searchedNotesFlow.value = NotesUiState.Success(it)
            }
        }
    }

    fun deleteNoteById(noteId: ObjectId) {
        viewModelScope.launch { MongoDB.deleteNoteById(noteId) }
    }

    private suspend fun getAllNotes() {
        MongoDB.getNotes().collectLatest {
            _notesFlow.value = NotesUiState.Success(it)
        }
    }

    fun getNoteById(noteId: ObjectId): Note? {
        return MongoDB.getNoteById(noteId)
    }

    fun deleteAllNotes() {
        viewModelScope.launch { MongoDB.deleteAllNotes() }
    }

    fun updateNote(id: ObjectId, title: String, text: String, image: String) {
        viewModelScope.launch {
            MongoDB.updateNote(id, title, text, image)
        }
    }

    fun insertNote(title: String, text: String, image: String) {
        viewModelScope.launch {
            MongoDB.createNote(title, text, image)
        }
    }

}