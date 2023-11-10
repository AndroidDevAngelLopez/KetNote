package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.ui.screen.utils.NoteJobUiState
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val handleNotesUseCase: HandleNotesUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    val connectivityStatusFlow: StateFlow<ConnectivityObserver.Status> =
        connectivityUseCase().stateIn(
            scope = viewModelScope,
            initialValue = ConnectivityObserver.Status.Unavailable,
            started = SharingStarted.WhileSubscribed(5_000)
        )
    val newIsNoteJobDone = handleNotesUseCase.isNoteJobDone.stateIn(
        scope = viewModelScope,
        initialValue = NoteJobUiState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val newCurrentNoteState = handleNotesUseCase.noteUiState.stateIn(
        scope = viewModelScope, initialValue = NoteUiState(), started = SharingStarted.Eagerly
    )

    fun updateCurrentJobDone(value: Boolean) {
        handleNotesUseCase.updateIsNoteJobDone(value)
    }

    fun updateCurrentState(title: String, text: String, image: Uri) {
        handleNotesUseCase.updateNoteUiStateFlow(title, text, image.toString())
    }

    fun deleteNote(noteId: ObjectId) {
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

    private fun createNote(title: String, text: String, image: String) {
        viewModelScope.launch {
            handleNotesUseCase.insertNote(
                title = title, text = text, image = image
            )
        }.invokeOnCompletion {
            Log.d("inserted!", "inserted!")
            handleNotesUseCase.updateIsNoteJobDone(true)
        }
    }

    fun insertNote(uploadTask: StorageReference, noteUiState: NoteUiState) {
        if (uploadTask.path != "/") {
            handleNotesUseCase.uploadPhotoToFirebase(uploadTask, noteUiState.image) {
                createNote(noteUiState.title, noteUiState.text, it)
            }
        } else {
            createNote(noteUiState.title, noteUiState.text, "")
        }
    }

    fun updateNote(
        currentNote: Note, uploadTask: StorageReference
    ) {
        val noteUiState = newCurrentNoteState.value
        if (currentNote.images.isNotEmpty()) {
            if (noteUiState.image.isNotEmpty()) {
                if (uploadTask.path != "/") {
                    deletePhoto(currentNote) {
                        uploadPhoto(uploadTask, Uri.parse(noteUiState.image)) {
                            viewModelScope.launch {
                                handleNotesUseCase.updateNote(
                                    currentNote._id, noteUiState.title, noteUiState.text, it
                                )
                                handleNotesUseCase.updateIsNoteJobDone(true)
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        handleNotesUseCase.updateNote(
                            currentNote._id, noteUiState.title, noteUiState.text, noteUiState.image
                        )
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            } else {
                deletePhoto(currentNote) {
                    viewModelScope.launch {
                        handleNotesUseCase.updateNote(
                            currentNote._id,
                            noteUiState.title,
                            noteUiState.text,
                            Uri.EMPTY.toString()
                        )
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            }
        } else {
            if (noteUiState.image.isNotEmpty()) {
                uploadPhoto(uploadTask, Uri.parse(noteUiState.image)) {
                    viewModelScope.launch {
                        handleNotesUseCase.updateNote(
                            currentNote._id, noteUiState.title, noteUiState.text, it
                        )
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    handleNotesUseCase.updateNote(
                        currentNote._id, noteUiState.title, noteUiState.text, noteUiState.image
                    )
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        }
    }

    private fun uploadPhoto(
        uploadTask: StorageReference, image: Uri, onUploadPhotoCompletion: (String) -> Unit
    ) {
        handleNotesUseCase.uploadPhotoToFirebase(uploadTask, image.toString()) {
            onUploadPhotoCompletion(it)
        }
    }

    private fun deletePhoto(note: Note, onDeletePhotoCompletion: () -> Unit) {
        val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
        handleNotesUseCase.deletePhotoFromFirebase(toDeleteRef) {
            onDeletePhotoCompletion()
        }
    }

    fun getNote(objectId: ObjectId) : Note{
        return handleNotesUseCase.getNoteById(objectId)!!
    }
}