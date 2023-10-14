package com.complexsoft.ketnote.ui.screen.home

import android.net.Uri
import androidx.fragment.app.DialogFragment
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
import com.google.firebase.storage.StorageReference
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

    private val _searchedNotesFlow: MutableStateFlow<NotesState<List<Note>>> =
        MutableStateFlow(NotesState.Idle)
    val searchedNotesFlow: StateFlow<NotesState<List<Note>>> = _searchedNotesFlow

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
                }

                ConnectivityObserver.Status.Lost -> {
                    _connectivityStateFlow.value = ConnectivityObserver.Status.Lost
                }

            }

        }.launchIn(viewModelScope)
    }

//    private fun addImageToLocalDatabase(
//        remoteImagePath: String, imageUri: String, sessionUri: String
//    ) {
//        viewModelScope.launch {
//            handleNotesUseCase.addImageToLocalDatabase(remoteImagePath, imageUri, sessionUri)
//        }
//    }

//    fun uploadPhotoToFirebase(
//        uploadTask: StorageReference, image: Uri, onUriDownloadReceived: (String) -> Unit
//    ) {
//
//        handleNotesUseCase.uploadPhotoToFirebase(uploadTask, image.toString(), onUriDownloadReceived)
//    }
//
//    fun insertNewNote(uploadTask: StorageReference, title: String, text: String, image: Uri) {
//        if (connectivityStateFlow.value == ConnectivityObserver.Status.Unavailable || connectivityStateFlow.value == ConnectivityObserver.Status.Lost) {
//            addImageToLocalDatabase(uploadTask.path, image.toString(), "this session")
//            insertNote(
//                title, text, image.toString()
//            )
//        } else {
//            uploadPhotoToFirebase(uploadTask, image) {
//                insertNote(
//                    title, text, it
//                )
//            }
//        }
//    }


    suspend fun cleanUpImageFromLocalDatabase(imageId: Int) {
        handleNotesUseCase.cleanUpImageFromLocalDatabase(imageId)
    }

    suspend fun getAllImagesFromLocalDatabase() {
        handleNotesUseCase.getAllImagesFromLocalDatabase()
    }


    fun logout(activity: FragmentActivity) {
        logoutUseCase.logoutUser(activity)
    }

    fun deletePhotoFromFirebase(imageToDelete: StorageReference, onImageDeleted: () -> Unit) {
        handleNotesUseCase.deletePhotoFromFirebase(imageToDelete, onImageDeleted)
    }

    fun openPhotoPicker(activity: DialogFragment, onImagesFetched: (images: List<Uri>) -> Unit) =
        handleNotesUseCase.openPhotoPicker(
            activity, onImagesFetched
        )

    fun searchNotesByTitle(title: String) {
        viewModelScope.launch {
            _searchedNotesFlow.value = NotesState.Loading
            handleNotesUseCase.searchNotesByTitle(title).collectLatest {
                _searchedNotesFlow.value = it
            }
        }
    }

    fun deleteNoteById(noteId: ObjectId) {
        viewModelScope.launch { handleNotesUseCase.deleteNoteById(noteId) }
    }

    private suspend fun getAllNotes() {
        withContext(Dispatchers.Main) {
            _notesFlow.value = NotesState.Loading
        }
        handleNotesUseCase.getAllNotes().collectLatest {
            _notesFlow.value = it
        }
    }

    fun getNoteById(noteId: ObjectId): Note? {
        return handleNotesUseCase.getNoteById(noteId)
    }

    fun deleteAllNotes(storage: FirebaseStorage) {
        viewModelScope.launch { handleNotesUseCase.deleteAllNotes(storage) }
    }

    fun updateNote(id: ObjectId, title: String, text: String, image: String) {
        viewModelScope.launch {
            handleNotesUseCase.updateNote(id, title, text, image)
        }
    }

    fun insertNote(title: String, text: String, image: String) {
        viewModelScope.launch {
            handleNotesUseCase.insertNote(title, text, image)
        }
    }

    fun resetSearchedPosts() {
        _notesFlow.value = NotesState.Idle
    }

}