package com.complexsoft.ketnote.ui.screen.create

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.domain.usecases.CreateNoteUseCase
import com.complexsoft.ketnote.domain.usecases.GeminiUseCase
import com.complexsoft.ketnote.domain.usecases.HandleConnectivityUseCase
import com.complexsoft.ketnote.domain.usecases.OpenImageChooserUseCase
import com.complexsoft.ketnote.domain.usecases.ShareToInstagramUseCase
import com.complexsoft.ketnote.domain.usecases.SpeechToTextUseCase
import com.complexsoft.ketnote.domain.usecases.TextToSpeechUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateIsNoteJobDoneUseCase
import com.complexsoft.ketnote.domain.usecases.UpdateNoteStateUseCase
import com.complexsoft.ketnote.ui.screen.utils.NoteJobUiState
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateScreenViewModel @Inject constructor(
    private val updateIsNoteJobDoneUseCase: UpdateIsNoteJobDoneUseCase,
    private val updateNoteStateUseCase: UpdateNoteStateUseCase,
    private val createNoteUseCase: CreateNoteUseCase,
    private val openImageChooserUseCase: OpenImageChooserUseCase,
    private val speechToTextUseCase: SpeechToTextUseCase,
    private val textToSpeechUseCase: TextToSpeechUseCase,
    private val geminiUseCase: GeminiUseCase,
    connectivityUseCase: HandleConnectivityUseCase
) : ViewModel() {

    /** USED FOR PARENT FRAGMENT (CREATE TABS FRAGMENT)*/
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

    /** USED FOR CREATE NOTE TAB*/
    val currentNoteState = updateNoteStateUseCase.noteUiState.stateIn(
        scope = viewModelScope,
        initialValue = NoteUiState(),
        started = SharingStarted.WhileSubscribed(5_000)
    )

    fun openPicker(fragment: Fragment, onUriReturn: (Uri) -> Unit) = openImageChooserUseCase(
        fragment, onUriReturn
    )


    fun configureTextToSpeech(fragment: Fragment) = textToSpeechUseCase(fragment)

    fun openSpeechToText(fragment: Fragment, onTextFetched: (String) -> Unit) =
        speechToTextUseCase(fragment, onTextFetched)

    fun openGemini(fragment: Fragment, imageToLoad: String, onResponse: (String) -> Unit) =
        geminiUseCase(fragment, onResponse, imageToLoad)


    fun updateCurrentJobDone(value: Boolean) = updateIsNoteJobDoneUseCase(value)

    fun updateCurrentState(
        title: String, text: String, image: Uri = currentNoteState.value.image
    ) = updateNoteStateUseCase(title, text, image)

    fun clearCurrentNoteState() {
        updateCurrentState("", "", Uri.EMPTY)
    }

    fun createNote(uploadTask: StorageReference) {
        viewModelScope.launch {
            createNoteUseCase(uploadTask = uploadTask, currentNote = currentNoteState.value)
        }.invokeOnCompletion {
            clearCurrentNoteState()
            updateCurrentJobDone(true)
        }
    }
}