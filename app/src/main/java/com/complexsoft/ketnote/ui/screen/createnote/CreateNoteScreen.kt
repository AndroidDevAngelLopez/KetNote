package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.complexsoft.ketnote.databinding.CreateNoteScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.utils.NoteUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

@AndroidEntryPoint
class CreateNoteScreen : Fragment(R.layout.create_note_screen_layout) {
    private var uploadTask: StorageReference = Firebase.storage.reference.child("/")
    private var imageUri: Uri = Uri.EMPTY
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var binding: CreateNoteScreenLayoutBinding
    private val viewModel by viewModels<CreateNoteViewModel>()
    private val args: CreateNoteScreenArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        storage = Firebase.storage
        storageRef = storage.reference
        binding = CreateNoteScreenLayoutBinding.inflate(layoutInflater)
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

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                if (uri.toString().isNotEmpty()) {
                    val remoteImagePath =
                        "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${uri.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                    uploadTask = storageRef.child(remoteImagePath)
                    imageUri = uri
                    viewModel.updateCurrentState(
                        binding.noteTitle.text.toString(),
                        binding.noteText.text.toString(),
                        imageUri
                    )
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.newIsNoteJobDone.collectLatest {
                        if (it) {
                            findNavController().popBackStack()
                        }
                    }
                }
                launch {
                    if (args.id.isNotEmpty()) {
                        val noteee = viewModel.getNote(ObjectId(args.id))!!
                        viewModel.updateCurrentState(
                            noteee.title,
                            noteee.text,
                            Uri.parse(noteee.images)
                        )
                        viewModel.newCurrentNoteState.collectLatest {
                            binding.topAppBar.menu.findItem(R.id.delete_note_menu_option).isVisible =
                                true
                            binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible =
                                false
                            binding.noteSendButton.text = "Actualizar"
                            binding.topAppBar.title = "Actualizar Nota"
                            binding.noteTitle.setText(it.title)
                            binding.noteText.setText(it.text)
                            if (it.image.isNotEmpty()) {
                                noteWithImageUi(it)
                            } else {
                                noteWithoutImageUi(it)
                            }
                        }
                    } else {
                        createNoteUi()
                    }
                }
            }
        }
        return binding.root
    }

    private fun createNoteUi() {
        /** THIS SECTION IS FOR CREATE NEW NOTE */
        binding.topAppBar.menu.findItem(R.id.delete_note_menu_option).isVisible = false
        binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible = false
        binding.currentImageLayout.itemImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_image_menu_option -> {
                    Glide.with(requireContext()).load(R.drawable.addphoto)
                        .into(binding.currentImageLayout.itemImage)
                    uploadTask = storageRef.child("/")
                    imageUri = Uri.EMPTY
                    binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible = false
                    true
                }

                else -> {
                    true
                }
            }
        }
        binding.noteSendButton.setOnClickListener {
            if (binding.noteTitle.text.toString().isNotEmpty()) {
                val newNote = NoteUiState(
                    title = binding.noteTitle.text.toString(),
                    text = binding.noteText.text.toString(),
                    image = imageUri.toString()
                )
                viewModel.insertNote(
                    uploadTask, newNote
                )
            }
        }
    }

    private fun noteWithImageUi(noteUiState: NoteUiState) {
        /**LOAD IMAGE USING GLIDE*/
        Glide.with(requireContext()).load(noteUiState.image)
            .into(binding.currentImageLayout.itemImage)
        /**
         * SET VISIBILITY OF DELETE IMAGE MENU OPTION AND SET FUNCTIONALITY
         * */
        binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible = true
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_note_menu_option -> {
                    viewModel.deleteNote(ObjectId(args.id))
                    true
                }

                R.id.delete_image_menu_option -> {
                    Glide.with(requireContext()).load(R.drawable.addphoto)
                        .into(binding.currentImageLayout.itemImage)
                    uploadTask = storageRef.child("/")
                    imageUri = Uri.EMPTY
                    viewModel.updateCurrentState(
                        binding.noteTitle.text.toString(),
                        binding.noteText.text.toString(),
                        imageUri
                    )
                    binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible = false
                    true
                }

                else -> {
                    true
                }
            }
        }
        binding.noteSendButton.setOnClickListener {
            if (binding.noteTitle.text.toString().isNotEmpty()) {
                viewModel.updateCurrentState(
                    title = binding.noteTitle.text.toString(),
                    text = binding.noteText.text.toString(),
                    image = Uri.parse(noteUiState.image)
                )
                viewModel.updateNote(
                    ObjectId(args.id), noteUiState, uploadTask
                )
            }
        }
    }

    private fun noteWithoutImageUi(noteUiState: NoteUiState) {
        binding.topAppBar.menu.findItem(R.id.delete_image_menu_option).isVisible = false
        binding.currentImageLayout.itemImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_note_menu_option -> {
                    viewModel.deleteNote(ObjectId(args.id))
                    true
                }

                else -> {
                    true
                }
            }
        }
        binding.noteSendButton.setOnClickListener {
            if (binding.noteTitle.text.toString().isNotEmpty()) {
                viewModel.updateCurrentState(
                    binding.noteTitle.text.toString(),
                    binding.noteText.text.toString(),
                    Uri.parse(noteUiState.image)
                )
                viewModel.updateNote(
                    ObjectId(args.id), noteUiState, uploadTask
                )
            }
        }
    }

}