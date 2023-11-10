package com.complexsoft.ketnote.ui.screen.home

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.domain.usecases.LogoutUseCase
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val handleNotesUseCase: HandleNotesUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    val newNotesFlow: StateFlow<NotesUiState> = handleNotesUseCase.getAllNotes().map(
        NotesUiState::Success
    ).stateIn(
        scope = viewModelScope,
        initialValue = NotesUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val connectivityStatusFlow: StateFlow<ConnectivityObserver.Status> =
        connectivityUseCase().stateIn(
            scope = viewModelScope,
            initialValue = ConnectivityObserver.Status.Unavailable,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun logout(activity: FragmentActivity) {
        logoutUseCase.logoutUser(activity)
    }

    private fun deleteNote(noteId: ObjectId) {
        val note = handleNotesUseCase.getNoteById(noteId)
        if (note?.images?.isNotEmpty() == true) {
            val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
            handleNotesUseCase.deletePhotoFromFirebase(toDeleteRef) {
                viewModelScope.launch {
                    handleNotesUseCase.deleteNoteById(noteId)
                }.invokeOnCompletion {
                    Log.d("inserted!", "inserted!")
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        } else {
            viewModelScope.launch {
                handleNotesUseCase.deleteNoteById(noteId)
            }.invokeOnCompletion {
                Log.d("inserted!", "inserted!")
                handleNotesUseCase.updateIsNoteJobDone(true)
            }
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            handleNotesUseCase.getAllNotes().collectLatest { notes ->
                for (note in notes) {
                    deleteNote(note._id)
                }
            }
        }
    }
}