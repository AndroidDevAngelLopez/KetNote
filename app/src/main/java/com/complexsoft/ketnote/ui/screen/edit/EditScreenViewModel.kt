package com.complexsoft.ketnote.ui.screen.edit

import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.domain.usecases.DeletePhotoFromFirebaseUseCase
import com.complexsoft.ketnote.domain.usecases.GeminiUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.OpenImageChooserUseCase
import com.complexsoft.ketnote.domain.usecases.ShareToInstagramUseCase
import com.complexsoft.ketnote.domain.usecases.SpeechToTextUseCase
import com.complexsoft.ketnote.domain.usecases.TextToSpeechUseCase
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
class EditScreenViewModel @Inject constructor(
    connectivityUseCase: HandleConnectivityUseCase,
    private val updateIsNoteJobDoneUseCase: UpdateIsNoteJobDoneUseCase,
    private val updateNoteStateUseCase: UpdateNoteStateUseCase,
    private val deletePhotoFromFirebaseUseCase: DeletePhotoFromFirebaseUseCase,
    private val uploadPhotoToFirebaseUseCase: UploadPhotoToFirebaseUseCase,
    private val openImageChooserUseCase: OpenImageChooserUseCase,
    private val speechToTextUseCase: SpeechToTextUseCase,
    private val textToSpeechUseCase: TextToSpeechUseCase,
    private val shareToInstagramUseCase: ShareToInstagramUseCase,
    private val geminiUseCase: GeminiUseCase
) : ViewModel() {

    val connectivityStatusFlow: StateFlow<ConnectivityObserver.Status> =
        connectivityUseCase().stateIn(
            scope = viewModelScope,
            initialValue = ConnectivityObserver.Status.Unavailable,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    val isNoteJobDone = updateIsNoteJobDoneUseCase.isNoteJobDone.stateIn(
        scope = viewModelScope,
        initialValue = NoteJobUiState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val currentNoteState = updateNoteStateUseCase.noteUiState.stateIn(
        scope = viewModelScope, initialValue = NoteUiState(), started = SharingStarted.Eagerly
    )

    fun openPicker(fragment: Fragment, onUriReturn: (Uri) -> Unit) = openImageChooserUseCase(
        fragment, onUriReturn
    )


    fun configureTextToSpeech(fragment: Fragment) = textToSpeechUseCase(fragment)

    fun openSpeechToText(fragment: Fragment, onTextFetched: (String) -> Unit) =
        speechToTextUseCase(fragment, onTextFetched)

    fun shareToInstagram(fragment: Fragment, image: String) =
        shareToInstagramUseCase(image, fragment)

    fun clearCurrentNoteState() {
        updateCurrentState("", "", Uri.EMPTY)
    }

    fun updateCurrentJobDone(value: Boolean) = updateIsNoteJobDoneUseCase(value)

    fun openGemini(fragment: Fragment, imageToLoad: String, onResponse: (String) -> Unit) =
        geminiUseCase(fragment, onResponse, imageToLoad)

    fun updateCurrentState(
        title: String, text: String, image: Uri = currentNoteState.value.image
    ) = updateNoteStateUseCase(title, text, image)

    fun deleteNote(note: Note) {
        if (note.images.isNotEmpty()) {
            val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
            deletePhotoFromFirebaseUseCase(toDeleteRef) {
                viewModelScope.launch {
                    MongoDB.deleteNoteById(note._id)
                }.invokeOnCompletion {
                    Log.d("DELETED!", "DELETED!")
                    updateIsNoteJobDoneUseCase(true)
                }
            }
        } else {
            viewModelScope.launch {
                MongoDB.deleteNoteById(note._id)
            }.invokeOnCompletion {
                Log.d("DELETED!", "deleted")
                updateIsNoteJobDoneUseCase(true)
            }
        }
    }

    fun updateNote(
        currentNote: Note, uploadTask: StorageReference
    ) {
        if (currentNote.images.isNotEmpty()) {
            if (currentNoteState.value.image.toString().isNotEmpty()) {
                if (uploadTask.path != "/") {
                    deletePhoto(currentNote) {
                        uploadPhoto(uploadTask, currentNoteState.value.image) {
                            viewModelScope.launch {
                                MongoDB.updateNote(
                                    currentNote._id,
                                    currentNoteState.value.title,
                                    currentNoteState.value.text,
                                    it
                                )
                                updateIsNoteJobDoneUseCase(true)
                            }
                        }
                    }
                } else {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id,
                            currentNoteState.value.title,
                            currentNoteState.value.text,
                            currentNoteState.value.image.toString()
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            } else {
                deletePhoto(currentNote) {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id,
                            currentNoteState.value.title,
                            currentNoteState.value.text,
                            Uri.EMPTY.toString()
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            }
        } else {
            if (currentNoteState.value.image.toString().isNotEmpty()) {
                uploadPhoto(uploadTask, currentNoteState.value.image) {
                    viewModelScope.launch {
                        MongoDB.updateNote(
                            currentNote._id,
                            currentNoteState.value.title,
                            currentNoteState.value.text,
                            it
                        )
                        updateIsNoteJobDoneUseCase(true)
                    }
                }
            } else {
                viewModelScope.launch {
                    MongoDB.updateNote(
                        currentNote._id,
                        currentNoteState.value.title,
                        currentNoteState.value.text,
                        currentNoteState.value.image.toString()
                    )
                }.invokeOnCompletion {
                    updateIsNoteJobDoneUseCase(true)
                    Log.d(
                        "NOTA ACTUALZIADA",
                        "NOTA ACTUALIZADA !!! ${currentNote._id}, ${currentNoteState.value.title}, ${currentNoteState.value.text}, ${currentNoteState.value.image}"
                    )
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

    fun getNoteById(id: String) = MongoDB.getNoteById(ObjectId(id))
}