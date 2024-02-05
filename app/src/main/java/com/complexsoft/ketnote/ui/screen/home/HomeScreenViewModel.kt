package com.complexsoft.ketnote.ui.screen.home

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.domain.usecases.DeletePhotoFromFirebaseUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.HandleLogoutUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNotificationsButtonUseCase
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.complexsoft.ketnote.ui.screen.utils.NotificationsButtonState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import javax.inject.Inject


val Context.dataStore2: DataStore<Preferences> by preferencesDataStore(name = "home_language")
val languageSelected = stringPreferencesKey("languageSelected")

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val handleLogoutUseCase: HandleLogoutUseCase,
    private val deletePhotoFromFirebaseUseCase: DeletePhotoFromFirebaseUseCase,
    private val updateNotificationsButtonUseCase: UpdateNotificationsButtonUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    val newNotesFlow: StateFlow<NotesUiState> = MongoDB.getNotes().map(
        NotesUiState::Success
    ).stateIn(
        scope = viewModelScope,
        initialValue = NotesUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val notificationsButtonState = updateNotificationsButtonUseCase.notificationsUiState.stateIn(
        scope = viewModelScope,
        initialValue = NotificationsButtonState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    val connectivityStatusFlow: StateFlow<ConnectivityObserver.Status> =
        connectivityUseCase().stateIn(
            scope = viewModelScope,
            initialValue = ConnectivityObserver.Status.Unavailable,
            started = SharingStarted.WhileSubscribed(5_000)
        )

    fun logout(activity: FragmentActivity) {
        handleLogoutUseCase(activity)
    }

    fun updateNotificationsButtonState(value: Boolean) = updateNotificationsButtonUseCase(value)

    fun setAppLanguage(context: Context, value: String) {
        viewModelScope.launch {
            context.dataStore2.edit { settings ->
                settings[languageSelected] = value
            }
        }
    }

    private fun deleteNote(noteId: ObjectId) {
        val note = MongoDB.getNoteById(noteId)
        if (note?.images?.isNotEmpty() == true) {
            val toDeleteRef = note.images.let { Firebase.storage.getReferenceFromUrl(it) }
            deletePhotoFromFirebaseUseCase(toDeleteRef) {
                viewModelScope.launch {
                    MongoDB.deleteNoteById(noteId)
                }.invokeOnCompletion {
                    Log.d("inserted!", "inserted!")
                }
            }
        } else {
            viewModelScope.launch {
                MongoDB.deleteNoteById(noteId)
            }.invokeOnCompletion {
                Log.d("inserted!", "inserted!")
            }
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch {
            MongoDB.getNotes().collectLatest { notes ->
                for (note in notes) {
                    deleteNote(note._id)
                }
                cancel()
            }
        }
    }
}