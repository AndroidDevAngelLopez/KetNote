package com.complexsoft.ketnote.ui.screen.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SetupOnboardingLayoutBinding

class OnBoardingSetUp : Fragment(R.layout.setup_onboarding_layout) {

    private lateinit var binding: SetupOnboardingLayoutBinding
    private lateinit var onBoardingAdapter: OnBoardingAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SetupOnboardingLayoutBinding.inflate(layoutInflater)
        onBoardingAdapter = OnBoardingAdapter(this)
        binding.onboardingPager.adapter = onBoardingAdapter
        binding.onboardingPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.onboardingButton.text = "Siguiente"
                    }

                    1 -> {
                        binding.onboardingButton.text = "Siguiente"
                    }

                    2 -> {
                        binding.onboardingButton.text = "Comenzar!"
                    }
                }
            }
        })




        return binding.root
    }

}