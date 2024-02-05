package com.complexsoft.ketnote.domain.usecases

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class SpeechToTextUseCase {
    operator fun invoke(
        fragment: Fragment,
        onTextFetched: (String) -> Unit
    ): ActivityResultLauncher<Intent> {
        return fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val matches =
                    data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                onTextFetched(matches[0])
            }
        }
    }
}