package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.domain.usecases.DeletePhotoFromFirebaseUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateIsNoteJobDoneUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNoteStateUseCase
import com.complexsoft.ketnote.domain.usecases.UploadPhotoToFirebaseUseCase
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
    private val updateIsNoteJobDoneUseCase: UpdateIsNoteJobDoneUseCase,
    private val updateNoteStateUseCase: UpdateNoteStateUseCase,
    private val deletePhotoFromFirebaseUseCase: DeletePhotoFromFirebaseUseCase,
    private val uploadPhotoToFirebaseUseCase: UploadPhotoToFirebaseUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    val connectivityStatusFlow: StateFlow<ConnectivityObserver.Status> =
        connectivityUseCase().stateIn(
            scope = viewModelScope,
            initialValue = ConnectivityObserver.Status.Unavailable,
            started = SharingStarted.WhileSubscribed(5_000)
        )
    val newIsNoteJobDone = updateIsNoteJobDoneUseCase.isNoteJobDone.stateIn(
        scope = viewModelScope,
        initialValue = NoteJobUiState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val newCurrentNoteState = updateNoteStateUseCase.noteUiState.stateIn(
        scope = viewModelScope, initialValue = NoteUiState(), started = SharingStarted.Eagerly
    )

    fun updateCurrentJobDone(value: Boolean) = updateIsNoteJobDoneUseCase(value)

    fun updateCurrentState(title: String, text: String, image: Uri) =
        updateNoteStateUseCase(title, text, image.toString())

    fun deleteNote(noteId: ObjectId) {
        val note = MongoDB.getNoteById(noteId)
        if (note?.images?.isNotEmpty() == true) {
            val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
            deletePhotoFromFirebaseUseCase(toDeleteRef) {
                viewModelScope.launch {
                    MongoDB.deleteNoteById(noteId)
                }.invokeOnCompletion {
                    Log.d("inserted!", "inserted!")
                    updateIsNoteJobDoneUseCase(true)
                }
            }
        } else {
            viewModelScope.launch {
                MongoDB.deleteNoteById(noteId)
            }.invokeOnCompletion {
                Log.d("inserted!", "inserted!")
                updateIsNoteJobDoneUseCase(true)
            }
        }
    }

    private fun createNote(title: String, text: String, image: String) {
        viewModelScope.launch {
            MongoDB.createNote(
                currentTitle = title, currentText = text, image = image
            )
        }.invokeOnCompletion {
            Log.d("inserted!", "inserted!")
            updateIsNoteJobDoneUseCase(true)
        }
    }

    fun insertNote(uploadTask: StorageReference, noteUiState: NoteUiState) {
        if (uploadTask.path != "/") {
            uploadPhotoToFirebaseUseCase(uploadTask, noteUiState.image) {
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
                                MongoDB.updateNote(
                                    currentNote._id, noteUiState.title, noteUiState.text, it
                                )
                                updateIsNoteJobDoneUseCase(true)
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id, noteUiState.title, noteUiState.text, noteUiState.image
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            } else {
                deletePhoto(currentNote) {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id,
                            noteUiState.title,
                            noteUiState.text,
                            Uri.EMPTY.toString()
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            }
        } else {
            if (noteUiState.image.isNotEmpty()) {
                uploadPhoto(uploadTask, Uri.parse(noteUiState.image)) {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id, noteUiState.title, noteUiState.text, it
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    MongoDB.updateNote(
                        currentNote._id, noteUiState.title, noteUiState.text, noteUiState.image
                    )
                    updateIsNoteJobDoneUseCase(true)
                }
            }
        }
    }

    private fun uploadPhoto(
        uploadTask: StorageReference, image: Uri, onUploadPhotoCompletion: (String) -> Unit
    ) {
        uploadPhotoToFirebaseUseCase(uploadTask, image.toString()) {
            onUploadPhotoCompletion(it)
        }
    }

    private fun deletePhoto(note: Note, onDeletePhotoCompletion: () -> Unit) {
        val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
        deletePhotoFromFirebaseUseCase(toDeleteRef) {
            onDeletePhotoCompletion()
        }
    }

    fun getNote(objectId: ObjectId): Note {
        return MongoDB.getNoteById(objectId)!!
    }
}