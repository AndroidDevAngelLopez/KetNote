package com.complexsoft.ketnote.domain.usecases

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.TEXTTOSPEECH
import java.util.Locale

class TextToSpeechUseCase {
    operator fun invoke(fragment: Fragment): TextToSpeech {
        return TextToSpeech(fragment.requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = TEXTTOSPEECH.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(
                        fragment.requireContext(), "Langugage is not Supported!", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}