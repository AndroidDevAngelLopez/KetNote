package com.complexsoft.ketnote.ui.screen.home

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.network.MongoDBAPP
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainActivity
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

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
                this.view?.let {
                    Snackbar.make(it, "Are you sure to delete all notes?", Snackbar.LENGTH_LONG)
                        .setAnchorView(binding.createNoteButton).setAction(R.string.action_text) {
                            viewModel.deleteAllNotes()
                        }.show()
                }
                menuItem.isCheckable = false
            }
            if (menuItem.itemId == R.id.signout_item) {
                viewLifecycleOwner.lifecycleScope.launch {
                    runCatching {
                        MongoDBAPP.app.currentUser?.remove()
                    }.onSuccess {
                        Firebase.auth.signOut()
                        delay(800)
                        val intent =
                            requireActivity().baseContext.packageManager.getLaunchIntentForPackage(
                                requireActivity().baseContext.packageName
                            )
                        intent!!.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        Process.killProcess(Process.myPid())
                        exitProcess(0)
                    }.onFailure {
                        Log.d("LOGOUT FAILED", it.message.toString())
                    }
                }
                menuItem.isCheckable = false
            }
            activity.binding.drawerLayout.close()
            true
        }

        activity.binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    findNavController().navigate(R.id.action_homeScreen_to_searchScreen)
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