package com.complexsoft.ketnote.ui.screen.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.SetupOnboardingLayoutBinding
import com.complexsoft.ketnote.ui.screen.utils.adapters.OnBoardingAdapter

class OnBoardingSetUp : Fragment(R.layout.setup_onboarding_layout) {

    private lateinit var binding: SetupOnboardingLayoutBinding
    private lateinit var onBoardingAdapter: OnBoardingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SetupOnboardingLayoutBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.onboardingConstraint) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        val viewModel by activityViewModels<OnBoardingViewModel>()

        binding.onboardingButton.visibility = View.VISIBLE
        onBoardingAdapter = OnBoardingAdapter(this@OnBoardingSetUp)
        binding.onboardingPager.adapter = onBoardingAdapter
        binding.onboardingPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.onboardingButton.text = getString(R.string.siguiente)
                        binding.onboardingButton.setOnClickListener {
                            binding.onboardingPager.currentItem = 1
                        }
                    }

                    1 -> {
                        binding.onboardingButton.text = getString(R.string.siguiente)
                        binding.onboardingButton.setOnClickListener {
                            binding.onboardingPager.currentItem = 2
                        }
                    }

                    2 -> {
                        binding.onboardingButton.text = getString(R.string.comenzar)
                        binding.onboardingButton.setOnClickListener {
                            context?.let { it1 ->
                                run {
                                    viewModel.onBoardingComplete(it1)
                                    findNavController().navigate(R.id.action_onBoardingSetUp_to_loginScreen)
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}


