package com.complexsoft.ketnote.ui.screen.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnBoardingAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

//    private val _textButton = MutableStateFlow(TextButtonStateHolder())
//    val textButton: StateFlow<TextButtonStateHolder> = _textButton
//
//    data class TextButtonStateHolder(
//        val textButton: String = "Siguiente", val secondFrag: String = "Siguiente",val thirdFrag: String = "Comenzar"
//    )

//    var textButton  = "Siguiente"

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
             //   _textButton.value = TextButtonStateHolder(textButton = "Siguiente")
                FirstOnBoarding()
            }

            1 -> {
              //  _textButton.value = TextButtonStateHolder(textButton = "Siguiente")
                SecondOnBoarding()
            }

            2 -> {
               // _textButton.value = TextButtonStateHolder(textButton = "Comenzar")
                ThirdOnBoarding()
            }

            else -> Fragment()
        }
    }
}