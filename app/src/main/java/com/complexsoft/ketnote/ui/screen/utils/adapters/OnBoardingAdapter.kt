package com.complexsoft.ketnote.ui.screen.utils.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.complexsoft.ketnote.ui.screen.onboarding.screens.FirstOnBoarding
import com.complexsoft.ketnote.ui.screen.onboarding.screens.SecondOnBoarding
import com.complexsoft.ketnote.ui.screen.onboarding.screens.ThirdOnBoarding

class OnBoardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FirstOnBoarding()
            }

            1 -> {
                SecondOnBoarding()
            }

            2 -> {
                ThirdOnBoarding()
            }

            else -> Fragment()
        }
    }
}