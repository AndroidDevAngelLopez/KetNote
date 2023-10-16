package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val handleNotesUseCase: HandleNotesUseCase,
    private val connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    private val _connectivityStateFlow: MutableStateFlow<ConnectivityObserver.Status> =
        MutableStateFlow(ConnectivityObserver.Status.Unavailable)
    val connectivityStateFlow: StateFlow<ConnectivityObserver.Status> = _connectivityStateFlow

    private val _noteUiState = MutableStateFlow(NoteUiState())
    val noteUiState: StateFlow<NoteUiState> = _noteUiState


    private val _isNoteJobDone = MutableStateFlow(false)
    val isNoteJobDone: StateFlow<Boolean> = _isNoteJobDone

    init {
        viewModelScope.launch(Dispatchers.IO) {
            observeConnectivity()
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
                }

                ConnectivityObserver.Status.Lost -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Lost
                }

            }

        }.launchIn(viewModelScope)
    }

    fun updateCurrentState(title: String, text: String, image: Uri) {
        _noteUiState.update { currentUiState ->
            currentUiState.copy(
                title = title, text = text, image = image.toString()
            )
        }
    }

    fun getNote(noteId: ObjectId) {
        val note = handleNotesUseCase.getNoteById(noteId = noteId)
        if (note != null) {
            _noteUiState.update { currentUiState ->
                currentUiState.copy(
                    title = note.title, text = note.text, image = note.images
                )
            }
        }
    }

    fun insertNewImage(
        uploadTask: StorageReference,
        noteId: ObjectId? = null,
        uri: Uri,
        title: String,
        text: String
    ) {
        var ownerId = ""
        if (noteId != null) {
            ownerId = handleNotesUseCase.getNoteById(noteId)?.owner_id.toString()
            if (connectivityStateFlow.value == ConnectivityObserver.Status.Unavailable || connectivityStateFlow.value == ConnectivityObserver.Status.Lost) {
                viewModelScope.launch {
                    handleNotesUseCase.addImageToUpload(
                        uploadTask.path, uri.toString(), ownerId
                    )
                }
                updateCurrentState(
                    title, text, uri
                )
                updateCurrentNote(
                    noteId,
                    noteUiState.value.title,
                    noteUiState.value.text,
                    noteUiState.value.image,
                    true
                )
            } else {
                handleNotesUseCase.uploadPhotoToFirebase(
                    uploadTask, uri.toString()
                ) {
                    updateCurrentState(
                        title, text, Uri.parse(it)
                    )
                    updateCurrentNote(
                        noteId,
                        noteUiState.value.title,
                        noteUiState.value.text,
                        noteUiState.value.image,
                        true
                    )
                }
            }
        } else {
            if (connectivityStateFlow.value == ConnectivityObserver.Status.Unavailable || connectivityStateFlow.value == ConnectivityObserver.Status.Lost) {
                viewModelScope.launch {
                    handleNotesUseCase.addImageToUpload(
                        uploadTask.path, uri.toString(), ownerId
                    )
                }
                updateCurrentState(
                    title, text, uri
                )
                createNote()
            } else {
                handleNotesUseCase.uploadPhotoToFirebase(
                    uploadTask, uri.toString()
                ) {
                    updateCurrentState(
                        title, text, Uri.parse(it)
                    )
                    createNote()
                }
            }
        }


    }

    fun deleteImage(
        toDeleteRef: StorageReference, noteId: ObjectId, title: String, text: String
    ) {
        val ownerId = handleNotesUseCase.getNoteById(noteId)?.owner_id
        if (connectivityStateFlow.value == ConnectivityObserver.Status.Unavailable || connectivityStateFlow.value == ConnectivityObserver.Status.Lost) {
            viewModelScope.launch {
                if (ownerId != null) {
                    handleNotesUseCase.addImageToDelete(toDeleteRef.path, ownerId)
                }
            }
            updateCurrentState(
                title, text, Uri.EMPTY
            )
            updateCurrentNote(
                noteId,
                noteUiState.value.title,
                noteUiState.value.text,
                noteUiState.value.image,
                true
            )
        } else {
            handleNotesUseCase.deletePhotoFromFirebase(toDeleteRef) {
                updateCurrentState(
                    title, text, Uri.EMPTY
                )
                updateCurrentNote(
                    noteId,
                    noteUiState.value.title,
                    noteUiState.value.text,
                    noteUiState.value.image,
                    true
                )
            }
        }

    }

    fun deleteCurrentNote(noteId: ObjectId) {
        viewModelScope.launch {
            handleNotesUseCase.deleteNoteById(noteId)
        }.invokeOnCompletion {
            Log.d("inserted!", "inserted!")
            _isNoteJobDone.value = true
        }
    }

    fun updateCurrentNote(
        id: ObjectId, title: String, text: String, image: String, isUpdatingFromImage: Boolean
    ) {
        viewModelScope.launch {
            handleNotesUseCase.updateNote(id, title, text, image)
        }.invokeOnCompletion {
            if (!isUpdatingFromImage) {
                Log.d("updated!", "updated!")
                _isNoteJobDone.value = true
            }
        }
    }


    fun setEmptyNote() {
        _noteUiState.update { currentUiState ->
            currentUiState.copy(
                title = "", text = "", image = ""
            )
        }
    }

    fun createNote() {
        viewModelScope.launch {
            handleNotesUseCase.insertNote(
                title = noteUiState.value.title,
                text = noteUiState.value.text,
                image = noteUiState.value.image
            )
        }.invokeOnCompletion {
            Log.d("inserted!", "inserted!")
            _isNoteJobDone.value = true
        }
    }
}