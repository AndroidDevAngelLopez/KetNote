package com.complexsoft.ketnote.ui.screen.home

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
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
import com.complexsoft.ketnote.data.network.MongoDBAPP
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainActivity
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
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


        val header = activity.binding.mainNavigationView.getHeaderView(0)
        val profilePic = header.findViewById<ImageView>(R.id.drawer_layout_header_profile_pic)
        val profileName =
            header.findViewById<MaterialTextView>(R.id.drawer_layout_header_profile_name)
        Glide.with(this).load(Firebase.auth.currentUser?.photoUrl).into(profilePic)
        profileName.text = Firebase.auth.currentUser?.displayName



        activity.binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.delete_all_item) {
                context?.let {
                    MaterialAlertDialogBuilder(it).setTitle("Are you sure to delete all notes?")
                        .setMessage("All notes will be deleted !")
                        .setNeutralButton("Cancel") { dialog, which ->
                            // Respond to neutral button press
                            dialog.dismiss()
                        }
//                        .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
//                            // Respond to negative button press
//                        }
                        .setPositiveButton("delete notes") { dialog, which ->
                            viewModel.deleteAllNotes()
                        }.show()
                }
                menuItem.isCheckable = false
            }

            if (menuItem.itemId == R.id.signout_item) {
                context?.let {
                    MaterialAlertDialogBuilder(it).setTitle("Are you sure to logout?")
                        .setMessage("save your work before logout!")
                        .setNeutralButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }.setPositiveButton("Logout") { dialog, which ->
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
                        }.show()
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
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = notesAdapter
        }
        return binding.root
    }
}