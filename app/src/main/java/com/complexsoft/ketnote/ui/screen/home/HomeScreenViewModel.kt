package com.complexsoft.ketnote.ui.screen.home

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.domain.usecases.LogoutUseCase
import com.complexsoft.ketnote.ui.screen.utils.NotesState
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val handleNotesUseCase: HandleNotesUseCase,
    private val connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    private val _notesFlow: MutableStateFlow<NotesState<List<Note>>> =
        MutableStateFlow(NotesState.Idle)
    val notesFlow: StateFlow<NotesState<List<Note>>> = _notesFlow

    private val _connectivityStateFlow: MutableStateFlow<ConnectivityObserver.Status> =
        MutableStateFlow(ConnectivityObserver.Status.Unavailable)
    val connectivityStateFlow: StateFlow<ConnectivityObserver.Status> = _connectivityStateFlow

    init {
        viewModelScope.launch(Dispatchers.IO) {
            observeConnectivity()
            getAllNotes()
        }
    }

    private fun observeConnectivity() {
        connectivityUseCase().onEach {
            when (it) {
                ConnectivityObserver.Status.Unavailable -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Unavailable
                }

                ConnectivityObserver.Status.Losing -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Losing
                }

                ConnectivityObserver.Status.Available -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Available
                    uploadImages()
                }

                ConnectivityObserver.Status.Lost -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Lost
                }

            }

        }.launchIn(viewModelScope)
    }

    private fun uploadImages() {
        viewModelScope.launch {
            handleNotesUseCase.uploadLocalImages { uri, id ->
                viewModelScope.launch {
                    val note = handleNotesUseCase.getNoteById(ObjectId(id))
                    if (note != null) {
                        handleNotesUseCase.updateNote(
                            ObjectId(id), note.title, note.text, uri
                        )
                    }
                }
            }
        }
    }

    fun logout(activity: FragmentActivity) {
        logoutUseCase.logoutUser(activity)
    }

    private suspend fun getAllNotes() {
        withContext(Dispatchers.Main) {
            _notesFlow.value = NotesState.Loading
        }
        handleNotesUseCase.getAllNotes().collectLatest {
            _notesFlow.value = it
        }
    }

    fun deleteAllNotes(storage: FirebaseStorage) {
        viewModelScope.launch { handleNotesUseCase.deleteAllNotes(storage) }
    }
}