package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val handleNotesUseCase: HandleNotesUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

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

    private val newConnectivityObserver = connectivityUseCase().stateIn(
        scope = viewModelScope,
        initialValue = ConnectivityObserver.Status.Unavailable,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun getNote(noteId: ObjectId) = handleNotesUseCase.getNoteById(noteId = noteId)

    fun deleteNote(noteId: ObjectId) {
        val note = getNote(noteId)
        if (newConnectivityObserver.value == ConnectivityObserver.Status.Available) {
            if (note?.images?.isNotEmpty() == true) {
                val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
                handleNotesUseCase.deletePhotoFromFirebase(toDeleteRef) {
                    viewModelScope.launch {
                        handleNotesUseCase.deleteNoteById(noteId)
                    }.invokeOnCompletion {
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    handleNotesUseCase.deleteNoteById(noteId)
                }.invokeOnCompletion {
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        } else {
            if (note?.images?.isNotEmpty() == true) {
                if (!note.images.contains("content")) {
                    val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
                    viewModelScope.launch {
                        handleNotesUseCase.addImageToDelete(
                            toDeleteRef.path, note._id.toHexString()
                        )
                        handleNotesUseCase.deleteNoteById(noteId)
                    }.invokeOnCompletion {
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                } else {
                    viewModelScope.launch {
                        handleNotesUseCase.cleanImageToUpload(note.images)
                        handleNotesUseCase.deleteNoteById(noteId)
                    }.invokeOnCompletion {
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    handleNotesUseCase.deleteNoteById(noteId)
                }.invokeOnCompletion {
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        }

    }

    fun insertNote(uploadTask: StorageReference, noteUiState: NoteUiState) {
        if (newConnectivityObserver.value == ConnectivityObserver.Status.Available) {
            if (uploadTask.path != "/") {
                handleNotesUseCase.uploadPhotoToFirebase(uploadTask, noteUiState.image) {
                    viewModelScope.launch {
                        handleNotesUseCase.insertNote(noteUiState.title, noteUiState.text, it)
                    }.invokeOnCompletion {
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    handleNotesUseCase.insertNote(noteUiState.title, noteUiState.text, "")
                }.invokeOnCompletion {
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        } else {
            if (uploadTask.path != "/") {
                viewModelScope.launch {
                    handleNotesUseCase.addImageToUpload(uploadTask.path, noteUiState.image)
                    handleNotesUseCase.insertNote(
                        noteUiState.title, noteUiState.text, noteUiState.image
                    )
                }.invokeOnCompletion {
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            } else {
                viewModelScope.launch {
                    handleNotesUseCase.insertNote(noteUiState.title, noteUiState.text, "")
                }.invokeOnCompletion {
                    handleNotesUseCase.updateIsNoteJobDone(true)
                }
            }
        }
    }

    fun updateNote(
        currentNote: Note, uploadTask: StorageReference
    ) {
        val noteUiState = newCurrentNoteState.value
        if (newConnectivityObserver.value == ConnectivityObserver.Status.Available) {
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
                                currentNote._id,
                                noteUiState.title,
                                noteUiState.text,
                                noteUiState.image
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
        } else {
            if (currentNote.images.isNotEmpty() && !currentNote.images.contains("content")) {
                if (noteUiState.image.isNotEmpty()) {
                    if (uploadTask.path != "/") {
                        val toDeleteRef =
                            currentNote.images.let { Firebase.storage.getReferenceFromUrl(it) }
                        viewModelScope.launch {
                            handleNotesUseCase.addImageToDelete(
                                toDeleteRef.path,
                                currentNote._id.toHexString()
                            )
                            handleNotesUseCase.addImageToUpload(uploadTask.path, noteUiState.image)
                            handleNotesUseCase.updateNote(
                                currentNote._id,
                                noteUiState.title,
                                noteUiState.text,
                                noteUiState.image
                            )
                            handleNotesUseCase.updateIsNoteJobDone(true)
                        }
                    } else {
                        viewModelScope.launch {
                            handleNotesUseCase.updateNote(
                                currentNote._id,
                                noteUiState.title,
                                noteUiState.text,
                                noteUiState.image
                            )
                            handleNotesUseCase.updateIsNoteJobDone(true)
                        }
                    }
                } else {
                    val toDeleteRef =
                        currentNote.images.let { Firebase.storage.getReferenceFromUrl(it) }
                    viewModelScope.launch {
                        handleNotesUseCase.addImageToDelete(
                            toDeleteRef.path,
                            currentNote._id.toHexString()
                        )
                        handleNotesUseCase.updateNote(
                            currentNote._id,
                            noteUiState.title,
                            noteUiState.text,
                            Uri.EMPTY.toString()
                        )
                        handleNotesUseCase.updateIsNoteJobDone(true)
                    }

                }
            } else {
                if (noteUiState.image.isNotEmpty()) {
                    if (uploadTask.path != "/") {
                        viewModelScope.launch {
                            handleNotesUseCase.cleanImageToUpload(noteUiState.image)
                            handleNotesUseCase.addImageToUpload(uploadTask.path, noteUiState.image)
                            handleNotesUseCase.updateNote(
                                currentNote._id,
                                noteUiState.title,
                                noteUiState.text,
                                noteUiState.image
                            )
                            handleNotesUseCase.updateIsNoteJobDone(true)
                        }
                    } else {
                        viewModelScope.launch {
                            handleNotesUseCase.updateNote(
                                currentNote._id,
                                noteUiState.title,
                                noteUiState.text,
                                noteUiState.image
                            )
                            handleNotesUseCase.updateIsNoteJobDone(true)
                        }
                    }
                } else {
                    viewModelScope.launch {
                        handleNotesUseCase.cleanImageToUpload(noteUiState.image)
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
}