package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainActivity
import com.complexsoft.ketnote.ui.screen.components.createDialog
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.complexsoft.ketnote.utils.Constants.APP_VERSION
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<HomeScreenViewModel>()
        val notesAdapter = NoteAdapter(emptyList()) {
            val action =
                HomeScreenDirections.actionHomeScreenToCreateNoteScreen(it._id.toHexString())
            findNavController().navigate(action)
        }

        val activity = requireActivity() as MainActivity

        activity.binding.topAppBar.setNavigationOnClickListener {
            activity.binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        val header = activity.binding.mainNavigationView.getHeaderView(0)
        val profilePic = header.findViewById<ImageView>(R.id.drawer_layout_header_profile_pic)
        val profileName =
            header.findViewById<MaterialTextView>(R.id.drawer_layout_header_profile_name)
        Glide.with(this).load(Firebase.auth.currentUser?.photoUrl).into(profilePic)
        profileName.text = Firebase.auth.currentUser?.displayName

        activity.binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete_all_item -> {
                    createDialog(
                        this.context,
                        "Are you sure to delete all notes?",
                        "All notes will be deleted !",
                        "Cancel",
                        "delete notes"
                    ) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteAllNotes(Firebase.storage)
                        }
                    }?.show()
                    menuItem.isCheckable = false
                }

                R.id.generate_all_notes_pdf -> {


                    menuItem.isCheckable = false
                }

                R.id.about_item -> {
                    createDialog(
                        this.context,
                        "KetNote",
                        "KetNote is an ComplexSoftSolutions © product.\ncurrent version is : $APP_VERSION",
                        "",
                        "OK"
                    ) {}?.show()
                    menuItem.isCheckable = false
                }

                R.id.signout_item -> {
                    createDialog(
                        this.context,
                        "Are you sure to logout?",
                        "save your work before logout!",
                        "Cancel",
                        "Logout"
                    ) {
                        viewModel.logout(requireActivity())
                    }?.show()
                    menuItem.isCheckable = false
                }
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
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = notesAdapter
        }
        return binding.root
    }
}