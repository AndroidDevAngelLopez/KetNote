package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainActivity
import com.complexsoft.ketnote.ui.screen.home.adapters.NoteAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<HomeScreenViewModel>()
        // db password Y282lAEZODVckp3j
        val notesAdapter = NoteAdapter(emptyList()) {
            val action =
                HomeScreenDirections.actionHomeScreenToCreateNoteScreen(it._id.toHexString())
            findNavController().navigate(action)
        }

        val activity = requireActivity() as MainActivity

        activity.binding.topAppBar.setNavigationOnClickListener {
            activity.binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        activity.binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.delete_all_item) {
                Toast.makeText(context, "deleted all clicked", Toast.LENGTH_SHORT).show()
                viewModel.deleteAllNotes()
                menuItem.isCheckable = false
            }
//            if (menuItem.itemId == R.id.signout_item) {
//                Toast.makeText(context, "siginout clicked", Toast.LENGTH_SHORT).show()
//                viewModel.signOutWithMongoAtlas()
//                findNavController().popBackStack()
//                menuItem.isCheckable = false
//            }
            activity.binding.drawerLayout.close()
            true
        }

        activity.binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    true
                }

                else -> false
            }
        }

        binding.createNoteButton.setOnClickListener {
            val action = HomeScreenDirections.actionHomeScreenToCreateNoteScreen("")
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notesFlow.collectLatest { state ->
                    when (state) {
                        is HomeScreenViewModel.NotesUiState.Success -> {
                            notesAdapter.updateList(state.notes)
                        }

                        else -> {}
                    }
                }
            }
        }

        binding.homeRecycler.apply {
            layoutManager = LinearLayoutManager(this@HomeScreen.context)
            adapter = notesAdapter
        }
        return binding.root
    }
}