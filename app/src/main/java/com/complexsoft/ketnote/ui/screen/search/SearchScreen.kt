package com.complexsoft.ketnote.ui.screen.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SearchViewLayoutBinding
import com.complexsoft.ketnote.ui.screen.home.HomeScreenViewModel
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView.TransitionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SearchScreen : Fragment(R.layout.search_view_layout) {

    private lateinit var binding: SearchViewLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SearchViewLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<HomeScreenViewModel>()
        binding.searchBar.visibility = View.GONE
        binding.searchView.show()
        binding.searchView.addTransitionListener { searchView, previousState, newState ->
            if (newState === TransitionState.HIDDEN) {
                findNavController().popBackStack()
            }
        }
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchNotesByTitle(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Do Nothing
            }

        })

        val notesAdapter = NoteAdapter(emptyList()) {
            val action =
                SearchScreenDirections.actionSearchScreenToCreateNoteScreen(it._id.toHexString())
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchedNotesFlow.collectLatest { state ->
                    when (state) {
                        is HomeScreenViewModel.NotesUiState.Success -> {
                            notesAdapter.updateList(state.notes)
                        }

                        else -> {}
                    }
                }
            }
        }


        val divider = this.context?.let {
            MaterialDividerItemDecoration(
                it, LinearLayoutManager.VERTICAL /*or LinearLayoutManager.HORIZONTAL*/
            )
        }
        binding.searchRecycler.apply {
            layoutManager = LinearLayoutManager(this@SearchScreen.context)
            adapter = notesAdapter
            if (divider != null) {
                addItemDecoration(divider)
            }
        }

        return binding.root
    }

}