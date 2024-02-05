package com.complexsoft.ketnote.ui.screen.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.EmptyUI
import com.complexsoft.ketnote.ui.screen.components.createRadioButtonDialog
import com.complexsoft.ketnote.ui.screen.components.createSimpleDialog
import com.complexsoft.ketnote.ui.screen.components.switchConnectivityObserverLayoutColor
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.complexsoft.ketnote.ui.screen.utils.adapters.StoriesAdapter
import com.complexsoft.ketnote.ui.screen.utils.adapters.NotesAdapter
import com.complexsoft.ketnote.utils.Constants.APP_VERSION
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.UncontainedCarouselStrategy
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale


@AndroidEntryPoint
class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding
    private lateinit var selectedLanguage: String

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = HomeScreenLayoutBinding.bind(view)
        val viewModel by viewModels<HomeScreenViewModel>()
        val languageSelectedFlow: Flow<String> =
            requireContext().dataStore2.data.map { preferences ->
                preferences[languageSelected] ?: Locale.getDefault().language
            }.stateIn(scope = CoroutineScope(Dispatchers.IO), started = SharingStarted.Eagerly, "")
        val notesAdapter = NotesAdapter(emptyList()) {
            val action = HomeScreenDirections.actionHomeScreenToEditScreen(it._id.toHexString())
            findNavController().navigate(action)
        }
        val storiesAdapter = StoriesAdapter(emptyList()) {
            val action = HomeScreenDirections.actionHomeScreenToEditScreen(it._id.toHexString())
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

                R.id.settings_item -> {
                    fun getCheckedItem(): Int {
                        return when (selectedLanguage) {
                            "es-MX" -> 1
                            "en-US" -> 0
                            else -> {
                                2
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
                            viewModel.setAppLanguage(requireContext(), "es-MX")
                        },
                        onEnglishAction = {
                            viewModel.setAppLanguage(requireContext(), "en-US")
                        },
                        getCheckedItem()
                    )?.show()
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

                R.id.notifications -> {
                    val modalBottomSheet = HomeScreenNotifications()
                    modalBottomSheet.show(parentFragmentManager, HomeScreenNotifications.TAG)
                    viewModel.updateNotificationsButtonState(true)
                    true
                }

                else -> false
            }
        }

        binding.createNoteButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeScreen_to_createScreen)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.newNotesFlow.collectLatest { state ->
                        when (state) {
                            is NotesUiState.Success -> {
                                if (state.data.isNotEmpty()) {
                                    binding.homeDividerNotes.visibility = View.VISIBLE
                                    val newSubList = state.data.filter {
                                        it.images.isNotEmpty()
                                        it.text.isEmpty()
                                    }
                                    if (newSubList.size <= 5) {
                                        storiesAdapter.updateList(newSubList)
                                    } else {
                                        storiesAdapter.updateList(newSubList.subList(0, 5))
                                    }
                                    if (state.data.size <= 5) {
                                        notesAdapter.updateList(state.data.minus(newSubList.toSet()))
                                    } else {
                                        notesAdapter.updateList(
                                            state.data.minus(newSubList.toSet()).subList(0, 5)
                                        )
                                    }
                                    binding.homeScreenMessage.visibility = View.GONE
                                    binding.homeScreenProgressIndicator.visibility = View.GONE
                                } else {
                                    binding.homeDividerNotes.visibility = View.GONE
                                    EmptyUI(
                                        fragment = this@HomeScreen,
                                        homeScreenLayoutBinding = binding
                                    )
                                    storiesAdapter.updateList(emptyList())
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
                launch {
                    languageSelectedFlow.collectLatest {
                        selectedLanguage = it
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(it)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                }
                launch {
                    viewModel.notificationsButtonState.collectLatest {
                        if (it.isClicked) {
                            binding.topAppBar.menu[1].icon?.setTint(
                                resources.getColor(
                                    R.color.md_theme_light_error, resources.newTheme()
                                )
                            )
                        } else {
                            binding.topAppBar.menu[1].icon?.setTint(
                                resources.getColor(R.color.seed, resources.newTheme())
                            )
                        }
                    }
                }
            }
        }
        binding.homeStoriesRecycler.apply {
            val carouselLayoutManager = CarouselLayoutManager(UncontainedCarouselStrategy())
            carouselLayoutManager.carouselAlignment = CarouselLayoutManager.ALIGNMENT_CENTER
            layoutManager = carouselLayoutManager
            adapter = storiesAdapter
        }
        val snapHelper = CarouselSnapHelper()
        snapHelper.attachToRecyclerView(binding.homeStoriesRecycler)
        binding.homeNotesRecycler.apply {
            layoutManager =
                if (computeWindowSizeClasses().heightWindowSizeClass == WindowHeightSizeClass.COMPACT) {
                    StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                } else {
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                }
            adapter = notesAdapter
        }
    }


    data class CurrentWindowSizes(
        val widthWindowSizeClass: WindowWidthSizeClass,
        val heightWindowSizeClass: WindowHeightSizeClass
    )

    private fun computeWindowSizeClasses(): CurrentWindowSizes {
        val metrics =
            WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(requireActivity())
        val width = metrics.bounds.width()
        val height = metrics.bounds.height()
        val density = resources.displayMetrics.density
        val windowSizeClass = WindowSizeClass.compute(width / density, height / density)
        // COMPACT, MEDIUM, or EXPANDED
        val widthWindowSizeClass = windowSizeClass.windowWidthSizeClass
        // COMPACT, MEDIUM, or EXPANDED
        val heightWindowSizeClass = windowSizeClass.windowHeightSizeClass
        // Use widthWindowSizeClass and heightWindowSizeClass.
        return CurrentWindowSizes(widthWindowSizeClass, heightWindowSizeClass)
    }
}