package com.complexsoft.ketnote.ui.screen.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.complexsoft.ketnote.data.repository.MongoDB
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val searchQuery = savedStateHandle.getStateFlow(key = SEARCH_QUERY, initialValue = "")
    fun onSearchQueryChanged(query: String) {
        savedStateHandle[SEARCH_QUERY] = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val newSearchedNotesFlow = searchQuery.flatMapLatest {
        MongoDB.searchNotesByTitle(title = it).map(
            NotesUiState::Success
        )
    }.stateIn(
        scope = viewModelScope,
        initialValue = NotesUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000)
    )
}

private const val SEARCH_QUERY = "searchQuery"
