package com.complexsoft.ketnote.ui.screen.components

import android.content.Context
import androidx.core.content.ContextCompat
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.ConnectivityObserverLayoutBinding

fun switchConnectivityObserverLayoutColor(
    context: Context,
    isAvailable: Boolean,
    loginConnectivityLayout: ConnectivityObserverLayoutBinding
) {
    if (isAvailable) {
        context.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.md_theme_light_tertiary
            )
        }.let { it2 ->
            loginConnectivityLayout.connectivityLayout.setCardBackgroundColor(
                it2
            )
        }
    } else {
        context.let { it1 ->
            ContextCompat.getColor(
                it1, R.color.md_theme_light_error
            )
        }.let { it2 ->
            loginConnectivityLayout.connectivityLayout.setCardBackgroundColor(
                it2
            )
        }
    }
}