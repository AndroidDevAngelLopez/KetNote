package com.complexsoft.ketnote.domain.usecases

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.BuildConfig
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.TEXTTOSPEECH
import com.complexsoft.ketnote.utils.Constants.GEMINI_IMAGE_MODEL
import com.complexsoft.ketnote.utils.Constants.GEMINI_MODEL
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeminiUseCase {

    operator fun invoke(
        fragment: Fragment, onResponseFetched: (String) -> Unit, imageToLoad: String = ""
    ): ActivityResultLauncher<Intent> {
        return fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val matches =
                    data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) as ArrayList<String>
                if (imageToLoad.isNotEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val image =
                            Glide.with(fragment.requireContext()).asBitmap().load(imageToLoad)
                                .submit().get()
                        val generativeModel = GenerativeModel(
                            // For text-and-image input (multimodal), use the gemini-pro-vision model
                            modelName = GEMINI_IMAGE_MODEL,
                            // Access your API key as a Build Configuration variable (see "Set up your API key" above)
                            apiKey = BuildConfig.GEMINI_API_KEY
                        )
                        var fullResponse = ""
                        val inputContent = content {
                            image(image)
                            text(matches[0])
                        }

                        generativeModel.generateContentStream(inputContent).collect { chunk ->
                            fullResponse += chunk.text
                        }
                        TEXTTOSPEECH.speak(
                            fullResponse, TextToSpeech.QUEUE_FLUSH, null, null
                        )
                        onResponseFetched(fullResponse)
                    }
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val generativeModel = GenerativeModel(
                            modelName = GEMINI_MODEL, apiKey = BuildConfig.GEMINI_API_KEY
                        )
                        val response = generativeModel.generateContent(matches[0])
                        TEXTTOSPEECH.speak(
                            response.text, TextToSpeech.QUEUE_FLUSH, null, null
                        )
                        response.text?.let { onResponseFetched(it) }
                    }
                }
            }
        }
    }
}