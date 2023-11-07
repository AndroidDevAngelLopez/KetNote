package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
import com.complexsoft.ketnote.ui.screen.components.createDialog
import com.complexsoft.ketnote.ui.screen.utils.NotesUiState
import com.complexsoft.ketnote.ui.screen.utils.adapters.NoteAdapter
import com.complexsoft.ketnote.utils.Constants.APP_VERSION
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding
    /*
    *
    * Migrate Flows,DI(also inject scopes,dispatchers)
    *
    * */

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
                    createDialog(
                        this.context,
                        "¿Estas seguro de borrar todas las notas?",
                        "Todas las notas seran eliminadas!",
                        "Cancelar",
                        "Eliminar notas"
                    ) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.deleteAllNotes(Firebase.storage)
                        }
                    }?.show()
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
                        "¿Estas seguro de cerrar sesion?",
                        "guarda tu trabajo antes de continuar!",
                        "Cancelar",
                        "Cerrar sesion"
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
                                    emptyUI()
                                    notesAdapter.updateList(emptyList())
                                }
                            }

                            is NotesUiState.Error -> {
                                emptyUI(message = state.error.message.toString())
                            }

                            is NotesUiState.Loading -> {
                                emptyUI(loading = true)
                            }
                        }
                    }
                }
                launch {
                    viewModel.connectivityStatusFlow.collectLatest {
                        when (it) {
                            ConnectivityObserver.Status.Unavailable -> {
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(false)
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    "Estas trabajando sin conexion"
                                delay(3000)
                                binding.homeConnectivityLayout.root.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Losing -> {
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(false)
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    "Estas perdiendo conexion!"
                                delay(2000)
                                binding.homeConnectivityLayout.root.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Available -> {
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(true)
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    "Sincronizando notas..."
                                delay(1200)
                                binding.homeConnectivityLayout.root.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Lost -> {
                                binding.homeConnectivityLayout.root.visibility = View.VISIBLE
                                switchConnectivityObserverLayoutColor(false)
                                binding.homeConnectivityLayout.connectivityLayoutMessage.text =
                                    "Estas trabajando sin conexion!"
                                delay(3000)
                                binding.homeConnectivityLayout.root.visibility = View.GONE
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

    private fun emptyUI(loading: Boolean = false, message: String = "No hay notas que mostrar") {
        if (loading) {
            binding.homeScreenProgressIndicator.visibility = View.VISIBLE
            binding.homeScreenMessage.visibility = View.GONE
        } else {
            binding.homeScreenProgressIndicator.visibility = View.GONE
            binding.homeScreenMessage.visibility = View.VISIBLE
            binding.homeScreenMessage.text = message
        }
    }

    private fun switchConnectivityObserverLayoutColor(isAvailable: Boolean) {
        if (isAvailable) {
            this@HomeScreen.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.md_theme_light_tertiary
                )
            }?.let { it2 ->
                binding.homeConnectivityLayout.connectivityLayout.setBackgroundColor(
                    it2
                )
            }
        } else {
            this@HomeScreen.context?.let { it1 ->
                ContextCompat.getColor(
                    it1, R.color.md_theme_dark_inversePrimary
                )
            }?.let { it2 ->
                binding.homeConnectivityLayout.connectivityLayout.setBackgroundColor(
                    it2
                )
            }
        }
    }
}