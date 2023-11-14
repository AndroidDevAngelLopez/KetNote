package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
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
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.EmptyUI
import com.complexsoft.ketnote.ui.screen.components.createRadioButtonDialog
import com.complexsoft.ketnote.ui.screen.components.createSimpleDialog
import com.complexsoft.ketnote.ui.screen.components.switchConnectivityObserverLayoutColor
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.complexsoft.ketnote.utils.Constants.APP_VERSION
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<HomeScreenViewModel>()
        val notesAdapter = NoteAdapter(emptyList()) {
            val action = HomeScreenDirections.actionHomeScreenToNewCreateNote(it._id.toHexString())
            findNavController().navigate(action)
        }

        binding.topAppBar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        val header = binding.mainNavigationView.getHeaderView(0)
        val profilePic = header.findViewById<ImageView>(R.id.drawer_layout_header_profile_pic)
        val profileName =
            header.findViewById<MaterialTextView>(R.id.drawer_layout_header_profile_name)
        Glide.with(this).load(Firebase.auth.currentUser?.photoUrl).into(profilePic)
        profileName.text = Firebase.auth.currentUser?.displayName

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainCoordinatorLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        binding.mainNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete_all_item -> {
                    createSimpleDialog(
                        requireContext(),
                        getString(R.string.delete_all_notes),
                        getString(R.string.all_notes_will_be_deleted),
                        getString(R.string.cancel),
                        getString(R.string.delete_notes)
                    ) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteAllNotes()
                        }
                    }?.show()
                    menuItem.isCheckable = false
                }

                R.id.about_item -> {
                    createSimpleDialog(
                        requireContext(),
                        getString(R.string.app_name),
                        getString(R.string.app_version_text, APP_VERSION),
                        "",
                        getString(R.string.ok)
                    ) {}?.show()
                    menuItem.isCheckable = false
                }

                R.id.signout_item -> {
                    createSimpleDialog(
                        requireContext(),
                        getString(R.string.close_sesion),
                        getString(R.string.save_your_work),
                        getString(R.string.cancel),
                        getString(R.string.logout)
                    ) {
                        viewModel.logout(requireActivity())
                    }?.show()
                    menuItem.isCheckable = false
                }
            }
            binding.drawerLayout.close()
            true
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    findNavController().navigate(R.id.action_homeScreen_to_searchScreen)
                    true
                }

                R.id.settings -> {
                    fun getCheckedItem(): Int {
                        val firstLocale = when (Locale.getDefault().language) {
                            "en" -> 0
                            "es" -> 1
                            else -> {
                                2
                            }
                        }
                        return when (AppCompatDelegate.getApplicationLocales().toLanguageTags()) {
                            "en-US" -> 0
                            "es-MX" -> 1
                            else -> {
                                firstLocale
                            }
                        }
                    }
                    createRadioButtonDialog(
                        requireContext(),
                        getString(R.string.language),
                        getString(R.string.ok),
                        getString(R.string.spanish),
                        getString(R.string.english),
                        onSpanishAction = {
                            val appLocale: LocaleListCompat =
                                LocaleListCompat.forLanguageTags("es-MX")
                            AppCompatDelegate.setApplicationLocales(appLocale)
                        },
                        onEnglishAction = {
                            val appLocale: LocaleListCompat =
                                LocaleListCompat.forLanguageTags("en-US")
                            AppCompatDelegate.setApplicationLocales(appLocale)
                        },
                        getCheckedItem()
                    )?.show()
                    true
                }

                else -> false
            }
        }

        binding.createNoteButton.setOnClickListener {
            val action = HomeScreenDirections.actionHomeScreenToNewCreateNote("")
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.newNotesFlow.collectLatest { state ->
                        when (state) {
                            is NotesUiState.Success -> {
                                if (state.data.isNotEmpty()) {
                                    notesAdapter.updateList(state.data)
                                    binding.homeScreenMessage.visibility = View.GONE
                                    binding.homeScreenProgressIndicator.visibility = View.GONE
                                } else {
                                    EmptyUI(
                                        fragment = this@HomeScreen,
                                        homeScreenLayoutBinding = binding
                                    )
                                    notesAdapter.updateList(emptyList())
                                }
                            }

                            is NotesUiState.Error -> {
                                EmptyUI(
                                    fragment = this@HomeScreen,
                                    message = state.error.message.toString(),
                                    homeScreenLayoutBinding = binding
                                )
                            }

                            is NotesUiState.Loading -> {
                                EmptyUI(
                                    fragment = this@HomeScreen,
                                    loading = true,
                                    homeScreenLayoutBinding = binding
                                )
                            }
                        }
                    }
                }
                launch {
                    viewModel.connectivityStatusFlow.collectLatest {
                        when (it) {
                            ConnectivityObserver.Status.Unavailable -> {
                                binding.mainNavigationView.menu.findItem(R.id.delete_all_item).isVisible =
                                    false
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.homeConnectivityLayout
                                )
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                binding.homeConnectivityLayout.root.setOnClickListener {
                                    binding.homeConnectivityLayout.root.visibility = View.GONE
                                }
                                binding.createNoteButton.visibility = View.GONE
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.no_internet_signal)
                            }

                            ConnectivityObserver.Status.Losing -> {
                                binding.mainNavigationView.menu.findItem(R.id.delete_all_item).isVisible =
                                    false
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.homeConnectivityLayout
                                )
                                binding.homeConnectivityLayout.root.setOnClickListener {
                                    binding.homeConnectivityLayout.root.visibility = View.GONE
                                }
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                binding.createNoteButton.visibility = View.GONE
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.losing_internet_signal)
                            }

                            ConnectivityObserver.Status.Available -> {
                                binding.mainNavigationView.menu.findItem(R.id.delete_all_item).isVisible =
                                    true
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), true, binding.homeConnectivityLayout
                                )
                                binding.createNoteButton.visibility = View.VISIBLE
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.loading_notes)
                                delay(1200)
                                binding.homeConnectivityLayout.root.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Lost -> {
                                binding.mainNavigationView.menu.findItem(R.id.delete_all_item).isVisible =
                                    false
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.homeConnectivityLayout
                                )
                                binding.homeConnectivityLayout.root.setOnClickListener {
                                    binding.homeConnectivityLayout.root.visibility = View.GONE
                                }
                                binding.createNoteButton.visibility = View.GONE
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.no_internet_signal)
                            }
                        }
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