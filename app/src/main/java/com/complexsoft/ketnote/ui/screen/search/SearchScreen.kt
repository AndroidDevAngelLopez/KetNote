package com.complexsoft.ketnote.ui.screen.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SearchViewLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.EmptyUI
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.complexsoft.ketnote.ui.screen.utils.adapters.SearchedNotesAdapter
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView.TransitionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchScreen : Fragment(R.layout.search_view_layout) {

    private lateinit var binding: SearchViewLayoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SearchViewLayoutBinding.bind(view)
        val viewModel by viewModels<SearchScreenViewModel>()
        binding.searchView.show()
        ViewCompat.setOnApplyWindowInsetsListener(binding.searchConstraint) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
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
                viewModel.onSearchQueryChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Do Nothing
            }

        })

        val notesAdapter = SearchedNotesAdapter(emptyList()) {
            val action = SearchScreenDirections.actionSearchScreenToEditScreen(it._id.toHexString())
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.newSearchedNotesFlow.collectLatest { state ->
                    when (state) {
                        is NotesUiState.Success -> {
                            if (state.data.isNotEmpty()) {
                                notesAdapter.updateList(state.data)
                                binding.searchScreenMessage.visibility = View.GONE
                                binding.searchScreenProgressIndicator.visibility = View.GONE
                            } else {
                                notesAdapter.updateList(emptyList())
                                EmptyUI(
                                    fragment = this@SearchScreen,
                                    searchScreenLayoutBinding = binding
                                )
                            }
                        }

                        is NotesUiState.Error -> {
                            EmptyUI(
                                fragment = this@SearchScreen,
                                searchScreenLayoutBinding = binding,
                                message = state.error.message.toString()
                            )
                        }

                        is NotesUiState.Loading -> {
                            EmptyUI(
                                fragment = this@SearchScreen,
                                searchScreenLayoutBinding = binding,
                                loading = true
                            )
                        }
                    }
                }
            }
        }

        val divider = this.context?.let {
            MaterialDividerItemDecoration(
                it, StaggeredGridLayoutManager.VERTICAL
            )
        }
        binding.searchRecycler.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = notesAdapter
            if (divider != null) {
                addItemDecoration(divider)
            }
        }
    }
}