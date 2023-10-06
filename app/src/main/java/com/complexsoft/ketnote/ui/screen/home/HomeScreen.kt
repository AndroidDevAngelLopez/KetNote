package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.HomeScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainViewModel
import com.complexsoft.ketnote.ui.screen.dataStore
import com.complexsoft.ketnote.ui.screen.getUserDataProfilePic
import com.complexsoft.ketnote.ui.screen.getUserDataToken
import com.complexsoft.ketnote.ui.screen.getUserDataUserName
import com.complexsoft.ketnote.ui.screen.login.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeScreen : Fragment(R.layout.home_screen_layout) {

    private lateinit var binding: HomeScreenLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = HomeScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by activityViewModels<MainViewModel>()
        val userSettingsFlow: Flow<User>? =
            activity?.applicationContext?.dataStore?.data?.map { preferences ->
                val userTokenId = preferences[getUserDataToken] ?: ""
                val username = preferences[getUserDataUserName] ?: ""
                val userpic = preferences[getUserDataProfilePic] ?: ""
                User(userTokenId, username, userpic)
            }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userSettingsFlow?.collectLatest {
                    //binding.materialTextView.text = it.username + it.userImage
                }
            }
        }
        return binding.root
    }
}