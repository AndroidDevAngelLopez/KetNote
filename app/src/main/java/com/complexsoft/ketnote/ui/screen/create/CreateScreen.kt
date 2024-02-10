package com.complexsoft.ketnote.ui.screen.create

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.databinding.CreateScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.switchConnectivityObserverLayoutColor
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.PICKMEDIA
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.TEXTTOSPEECH
import com.complexsoft.ketnote.ui.screen.utils.UIConstants.UPLOADTASK
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateScreen : Fragment(R.layout.create_screen_layout) {
    private lateinit var binding: CreateScreenLayoutBinding
    private val viewModel by viewModels<CreateScreenViewModel>()
    private var isImageClicked = false
    private var isGeminiButtonClicked: Boolean = false

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearCurrentNoteState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = CreateScreenLayoutBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.createNoteCoordinator) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }

        TEXTTOSPEECH = viewModel.configureTextToSpeech(this)
        val speechToTextLauncher = viewModel.openSpeechToText(this) {
            if (binding.noteTitle.isFocused) binding.noteTitle.setText(it)
            else if (binding.noteText.isFocused) binding.noteText.setText(
                it
            )
        }
        val speechToGeminiLauncher = viewModel.openGemini(this, "") {
            CoroutineScope(Dispatchers.Main).launch {
                if (binding.noteTitle.isFocused) binding.noteTitle.setText(it)
                else if (binding.noteText.isFocused) binding.noteText.setText(
                    it
                ) else Toast.makeText(
                    requireContext(),
                    "You Must select either title or text to set generated text!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        PICKMEDIA = viewModel.openPicker(this) {
            viewModel.updateCurrentState(
                title = binding.noteTitle.text.toString(),
                text = binding.noteText.text.toString(),
                image = it
            )
            isImageClicked = true
        }

        binding.topAppBar.setNavigationOnClickListener {
            viewModel.clearCurrentNoteState()
            findNavController().popBackStack()
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.create_voice_control -> {
                    val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    speechIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Complete With Voice")
                    speechToTextLauncher.launch(speechIntent)
                    true
                }

                R.id.create_image_control -> {
                    isImageClicked = !isImageClicked
                    if (isImageClicked) {
                        binding.currentImageLayout.root.visibility = View.VISIBLE
                    } else {
                        binding.currentImageLayout.itemImage.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.addphoto, resources.newTheme()
                            )
                        )
                        viewModel.updateCurrentState(
                            title = binding.noteTitle.text.toString(),
                            text = binding.noteText.text.toString(),
                            image = Uri.EMPTY
                        )
                        UPLOADTASK = Firebase.storage.reference.child("/")
                        binding.currentImageLayout.root.visibility = View.GONE
                    }
                    true
                }

                R.id.create_gemini_control -> {
                    isGeminiButtonClicked = !isGeminiButtonClicked
                    if (isGeminiButtonClicked) {
                        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        speechIntent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Gemini")
                        speechToGeminiLauncher.launch(speechIntent)
                    }
                    true
                }

                else -> false
            }
        }

        binding.createScreenSendButton.setOnClickListener {
            if (binding.noteTitle.text.toString().isNotEmpty()) {
                viewModel.updateCurrentState(
                    title = binding.noteTitle.text.toString(),
                    text = binding.noteText.text.toString(),
                )
                viewModel.createNote(UPLOADTASK)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.must_insert_title), Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.newIsNoteJobDone.collectLatest {
                        if (it.value) {
                            viewModel.updateCurrentJobDone(false)
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.currentNoteState.collectLatest { noteUiState ->
                        if (noteUiState.image.toString().isNotEmpty()) {
                            Glide.with(this@CreateScreen).load(noteUiState.image)
                                .into(binding.currentImageLayout.itemImage)
                            binding.currentImageLayout.root.setOnClickListener {
                                val action =
                                    CreateScreenDirections.actionNewCreateNoteToImageVisorFragment(
                                        noteUiState.image.toString(),
                                        getString(R.string.image_preview)
                                    )
                                findNavController().navigate(action)
                            }
                        } else {
                            binding.currentImageLayout.root.setOnClickListener {
                                PICKMEDIA.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        }
                    }
                }
                launch {
                    viewModel.connectivityStatusFlow.collectLatest {
                        when (it) {
                            ConnectivityObserver.Status.Unavailable -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.createScreenConnectivityLayout
                                )
                                binding.bottomAppBar.visibility = View.GONE
                                binding.createScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.createScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.get_connection)
                                binding.createScreenSendButton.visibility = View.GONE

                            }

                            ConnectivityObserver.Status.Losing -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.createScreenConnectivityLayout
                                )
                                binding.createScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.createScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.losing_internet_signal)
                                binding.bottomAppBar.visibility = View.GONE
                                binding.createScreenSendButton.visibility = View.GONE

                            }

                            ConnectivityObserver.Status.Available -> {
                                binding.createScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.GONE
                                binding.bottomAppBar.visibility = View.VISIBLE
                                binding.createScreenSendButton.visibility = View.VISIBLE
                            }

                            ConnectivityObserver.Status.Lost -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.createScreenConnectivityLayout
                                )
                                binding.createScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.createScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.get_connection)
                                binding.bottomAppBar.visibility = View.GONE
                                binding.createScreenSendButton.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }
}