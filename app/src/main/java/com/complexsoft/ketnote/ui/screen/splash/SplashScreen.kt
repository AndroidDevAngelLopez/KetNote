package com.complexsoft.ketnote.ui.screen.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SplashScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.MainViewModel
import com.complexsoft.ketnote.ui.screen.dataStore
import com.complexsoft.ketnote.ui.screen.isOnBoardingCompleted
import com.complexsoft.ketnote.ui.screen.isUserLogged
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment(R.layout.splash_screen_layout) {
    private lateinit var binding: SplashScreenLayoutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SplashScreenLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<MainViewModel>()
        val isCompletedOnBoarding: Flow<Boolean>? =
            activity?.applicationContext?.dataStore?.data?.map { preferences ->
                preferences[isOnBoardingCompleted] ?: false
            }
        val isUserCompletedLogging: Flow<Boolean>? =
            activity?.applicationContext?.dataStore?.data?.map { preferences ->
                preferences[isUserLogged] ?: false
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(2000)
                isCompletedOnBoarding?.collectLatest { isCompleted ->
                    isUserCompletedLogging?.collectLatest { isLogged ->
                        if (isCompleted && isLogged) {
                            findNavController().navigate(R.id.action_splashScreen_to_homeScreen)
                        } else {
                            findNavController().navigate(R.id.action_splashScreen_to_onBoardingSetUp)
                        }
                    }
                }
            }
        }
        return binding.root
    }
}