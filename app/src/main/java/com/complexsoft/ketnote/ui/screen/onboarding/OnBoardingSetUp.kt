package com.complexsoft.ketnote.ui.screen.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SetupOnboardingLayoutBinding
import com.complexsoft.ketnote.ui.screen.utils.adapters.OnBoardingAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class OnBoardingSetUp : Fragment(R.layout.setup_onboarding_layout) {

    private lateinit var binding: SetupOnboardingLayoutBinding
    private lateinit var onBoardingAdapter: OnBoardingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SetupOnboardingLayoutBinding.inflate(layoutInflater)
        val viewModel by activityViewModels<OnBoardingViewModel>()
        onBoardingAdapter = OnBoardingAdapter(this)
        binding.onboardingPager.adapter = onBoardingAdapter
        binding.onboardingPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.onboardingButton.text = "Siguiente"
                        binding.onboardingButton.setOnClickListener {
                            binding.onboardingPager.currentItem = 1
                        }
                    }

                    1 -> {
                        binding.onboardingButton.text = "Siguiente"
                        binding.onboardingButton.setOnClickListener {
                            binding.onboardingPager.currentItem = 2
                        }
                    }

                    2 -> {
                        binding.onboardingButton.text = "Comenzar!"
                        binding.onboardingButton.setOnClickListener {
                            context?.let { it1 -> viewModel.onBoardingComplete(it1) }
                        }
                    }
                }
            }
        })
        val isOnBoardingCompleted: Flow<Boolean>? = context?.dataStore?.data?.map { preferences ->
            preferences[isOnBoardingCompleted] ?: false
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                isOnBoardingCompleted?.collectLatest {
                    if (it) {
                        findNavController().navigate(R.id.action_onBoardingSetUp_to_loginScreen)
                    }
                }
            }
        }
        return binding.root
    }
}


