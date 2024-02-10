package com.complexsoft.ketnote.ui.screen.edit

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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.network.connectivity.ConnectivityObserver
import com.complexsoft.ketnote.databinding.EditScreenLayoutBinding
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
import toHumanDate

@AndroidEntryPoint
class EditScreen : Fragment(R.layout.edit_screen_layout) {

    private var isImageSelectorOpen: Boolean = false
    private var isSendButtonClicked: Boolean = false
    private var isGeminiButtonClicked: Boolean = false
    private lateinit var binding: EditScreenLayoutBinding
    private val viewModel by viewModels<EditScreenViewModel>()
    private val args: EditScreenArgs by navArgs()

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearCurrentNoteState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = EditScreenLayoutBinding.bind(view)
        val noteFetched = viewModel.getNoteById(args.id)!!
        TEXTTOSPEECH = viewModel.configureTextToSpeech(this)

        if (noteFetched.images.isNotEmpty()) {
            binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram).setVisible(true)
        } else {
            binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram).setVisible(false)
        }

        val speechToTextLauncher = viewModel.openSpeechToText(this) {
            if (binding.editScreenTitle.isFocused) binding.editScreenTitle.setText(it)
            else if (binding.editScreenText.isFocused) binding.editScreenText.setText(
                it
            )
        }
        val speechToGeminiLauncher = viewModel.openGemini(this, noteFetched.images) {
            CoroutineScope(Dispatchers.Main).launch {
                if (binding.editScreenTitle.isFocused) binding.editScreenTitle.setText(it)
                else if (binding.editScreenText.isFocused) binding.editScreenText.setText(
                    it
                ) else Toast.makeText(
                    requireContext(),
                    "You Must select either title or text to set generated text!"
                ,Toast.LENGTH_SHORT).show()
            }
        }

        PICKMEDIA = viewModel.openPicker(this) {
            viewModel.updateCurrentState(
                title = binding.editScreenTitle.text.toString(),
                text = binding.editScreenText.text.toString(),
                image = it
            )
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.editCoordinatorLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        binding.editScreenTopAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.editScreenTopAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_delete_control -> {
                    viewModel.deleteNote(noteFetched)
                    true
                }

                R.id.edit_share_instagram -> {
                    viewModel.shareToInstagram(this, noteFetched.images)
                    true
                }

                else -> {
                    true
                }
            }
        }

        binding.editScreenBottomAppbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_voice_control -> {
                    val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    speechIntent.putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    speechIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Complete With Voice")
                    speechToTextLauncher.launch(speechIntent)
                    true
                }

                R.id.edit_image_control -> {
                    isImageSelectorOpen = !isImageSelectorOpen
                    if (isImageSelectorOpen) {
                        binding.editScreenImage.visibility = View.VISIBLE
                    } else {
                        binding.editScreenImage.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                resources, R.drawable.addphoto, resources.newTheme()
                            )
                        )
                        viewModel.updateCurrentState(
                            title = binding.editScreenTitle.text.toString(),
                            text = binding.editScreenText.text.toString(),
                            image = Uri.EMPTY
                        )
                        UPLOADTASK = Firebase.storage.reference.child("/")
                        binding.editScreenImage.visibility = View.GONE
                    }
                    true
                }

                R.id.edit_gemini_control -> {
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

        binding.editScreenSendButton.setOnClickListener {
            isSendButtonClicked = !isSendButtonClicked
            if (isSendButtonClicked) {
                viewModel.updateCurrentState(
                    title = binding.editScreenTitle.text.toString(),
                    text = binding.editScreenText.text.toString(),
                )
                if (binding.editScreenTitle.text.toString().isNotEmpty()) {
                    viewModel.updateNote(noteFetched, UPLOADTASK)
                } else {
                    Toast.makeText(
                        requireContext(), getString(R.string.must_insert_title), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.editScreenDate.text = noteFetched.date.toHumanDate()
        binding.editScreenTitle.setText(noteFetched.title)
        binding.editScreenText.setText(noteFetched.text)
        viewModel.updateCurrentState(
            noteFetched.title, noteFetched.text, Uri.parse(noteFetched.images)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isNoteJobDone.collectLatest {
                        if (it.value) {
                            viewModel.updateCurrentJobDone(false)
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    viewModel.currentNoteState.collectLatest { noteUiState ->
                        if (noteUiState.image.toString().isNotEmpty()) {
                            isImageSelectorOpen = true
                            binding.editScreenImage.visibility = View.VISIBLE
                            Glide.with(this@EditScreen).load(noteUiState.image)
                                .into(binding.editScreenImage)
                            binding.editScreenImage.setOnClickListener {
                                val action =
                                    EditScreenDirections.actionEditScreenToImageVisorFragment(
                                        noteUiState.image.toString(),
                                        getString(R.string.image_preview)
                                    )
                                findNavController().navigate(action)
                            }
                        } else {
                            isImageSelectorOpen = false
                            binding.editScreenImage.visibility = View.GONE
                            binding.editScreenImage.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources, R.drawable.addphoto, resources.newTheme()
                                )
                            )
                            binding.editScreenImage.setOnClickListener {
                                PICKMEDIA.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        }
                    }
                }
                launch {
                    viewModel.connectivityStatusFlow.collectLatest {
                        when (it) {
                            ConnectivityObserver.Status.Unavailable -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.editScreenConnectivityLayout
                                )
                                binding.editScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.editScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.get_connection)
                                binding.editScreenBottomAppbar.visibility = View.GONE
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram)
                                    .setVisible(false)
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_delete_control)
                                    .setVisible(false)
                                binding.editScreenSendButton.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Losing -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.editScreenConnectivityLayout
                                )
                                binding.editScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.editScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.get_connection)
                                binding.editScreenBottomAppbar.visibility = View.GONE
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram)
                                    .setVisible(false)
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_delete_control)
                                    .setVisible(false)
                                binding.editScreenSendButton.visibility = View.GONE
                            }

                            ConnectivityObserver.Status.Available -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), true, binding.editScreenConnectivityLayout
                                )
                                binding.editScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.GONE
                                binding.editScreenBottomAppbar.visibility = View.VISIBLE
                                if (noteFetched.images.isNotEmpty()) {
                                    binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram)
                                        .setVisible(true)
                                } else {
                                    binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram)
                                        .setVisible(false)
                                }

                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_delete_control)
                                    .setVisible(true)
                                binding.editScreenSendButton.visibility = View.VISIBLE
                            }

                            ConnectivityObserver.Status.Lost -> {
                                switchConnectivityObserverLayoutColor(
                                    requireContext(), false, binding.editScreenConnectivityLayout
                                )
                                binding.editScreenConnectivityLayout.connectivityLayout.visibility =
                                    View.VISIBLE
                                binding.editScreenConnectivityLayout.connectivityLayoutMessage.text =
                                    getString(R.string.get_connection)
                                binding.editScreenBottomAppbar.visibility = View.GONE
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_share_instagram)
                                    .setVisible(false)
                                binding.editScreenTopAppBar.menu.findItem(R.id.edit_delete_control)
                                    .setVisible(false)
                                binding.editScreenSendButton.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }
}