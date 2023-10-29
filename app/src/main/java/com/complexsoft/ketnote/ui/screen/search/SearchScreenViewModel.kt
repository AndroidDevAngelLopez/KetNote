package com.complexsoft.ketnote.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.domain.usecases.HandleNotesUseCase
import com.complexsoft.ketnote.ui.screen.utils.NotesState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SearchScreenViewModel(
    private val handleNotesUseCase: HandleNotesUseCase,
) : ViewModel() {

    private val _searchedNotesFlow: MutableStateFlow<NotesState<List<Note>>> =
        MutableStateFlow(NotesState.Idle)
    val searchedNotesFlow: StateFlow<NotesState<List<Note>>> = _searchedNotesFlow

    fun searchNotesByTitle(title: String) {
        viewModelScope.launch {
            _searchedNotesFlow.value = NotesState.Loading
            handleNotesUseCase.searchNotesByTitle(title).collectLatest {
                _searchedNotesFlow.value = it
            }
        }
    }
}