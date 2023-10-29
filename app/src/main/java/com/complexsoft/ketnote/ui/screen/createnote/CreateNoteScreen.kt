package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.ImageNote
import com.complexsoft.ketnote.databinding.CreateNoteScreenLayoutBinding
import com.complexsoft.ketnote.ui.screen.utils.adapters.ImageNoteAdapter
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
    private lateinit var uploadTask: StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var imageNoteAdapter: ImageNoteAdapter
    private lateinit var binding: CreateNoteScreenLayoutBinding
    private val viewModel by viewModels<CreateNoteViewModel>()
    private val args: CreateNoteScreenArgs by navArgs()
    private var isImagePickerOpened = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        storage = Firebase.storage
        storageRef = storage.reference
        binding = CreateNoteScreenLayoutBinding.inflate(layoutInflater)
        ViewCompat.setOnApplyWindowInsetsListener(binding.createNoteConstraint) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                topMargin = insets.top
                rightMargin = insets.right
                bottomMargin = insets.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
        imageNoteAdapter = ImageNoteAdapter(emptyList()) {
            if (it.src.isNotEmpty()) {
                val action =
                    CreateNoteScreenDirections.actionNewCreateNoteToImageVisorFragment(it.src)
                findNavController().navigate(action)
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    if (uri.toString().isNotEmpty()) {
                        imageNoteAdapter.updateList(listOf(ImageNote(src = uri.toString())))
                        val remoteImagePath =
                            "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${uri.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                        uploadTask = storageRef.child(remoteImagePath)
                        if (args.id.isNotBlank()) {
                            binding.noteImageRecyclerView.visibility = View.GONE
                            binding.noteAddImageButton.visibility = View.GONE
                            binding.createNoteProgressIndicator.visibility = View.VISIBLE
                            viewModel.insertNewImage(
                                uploadTask,
                                ObjectId(args.id),
                                uri,
                                binding.noteTitle.text.toString(),
                                binding.noteText.text.toString()
                            )
                            binding.createNoteProgressIndicator.visibility = View.GONE
                            binding.noteAddImageButton.visibility = View.VISIBLE
                            binding.noteImageRecyclerView.visibility = View.VISIBLE
                        } else {
                            binding.noteImageRecyclerView.visibility = View.GONE
                            binding.noteAddImageButton.visibility = View.GONE
                            binding.createNoteProgressIndicator.visibility = View.VISIBLE
                            viewModel.insertNewImage(
                                uploadTask = uploadTask,
                                uri = uri,
                                title = binding.noteTitle.text.toString(),
                                text = binding.noteText.text.toString()
                            )
                            binding.createNoteProgressIndicator.visibility = View.GONE
                            binding.noteAddImageButton.visibility = View.VISIBLE
                            binding.noteImageRecyclerView.visibility = View.VISIBLE
                        }

                        isImagePickerOpened = true
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }

        if (args.id.isNotBlank()) {
            viewModel.getNote(ObjectId(args.id))
        } else {
            viewModel.setEmptyNote()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.noteUiState.collectLatest { noteUIState ->
                        if (args.id.isNotEmpty()) {
                            binding.topAppBar.menu.findItem(R.id.delete_note_menu_option).isVisible =
                                true
                            binding.topAppBar.setOnMenuItemClickListener {
                                when (it.itemId) {
                                    R.id.delete_note_menu_option -> {
                                        if (noteUIState.image.isNotEmpty()) {
                                            if (!noteUIState.image.contains("content")) {
                                                val toDeleteRef =
                                                    storage.getReferenceFromUrl(noteUIState.image)
                                                viewModel.deleteImage(
                                                    toDeleteRef,
                                                    ObjectId(args.id),
                                                    binding.noteTitle.text.toString(),
                                                    binding.noteText.text.toString()
                                                )
                                            } else {
                                                viewModel.deleteCurrentNote(ObjectId(args.id))
                                            }
                                        }
                                        viewModel.deleteCurrentNote(ObjectId(args.id))
                                        true
                                    }

                                    else -> {
                                        true
                                    }
                                }
                            }
                            binding.noteSendButton.text = "Actualizar"
                            binding.topAppBar.title = "Actualizar Nota"
                            binding.noteSendButton.visibility = View.VISIBLE
                            binding.noteTitle.setText(noteUIState.title)
                            binding.noteText.setText(noteUIState.text)
                            binding.noteSendButton.setOnClickListener {
                                if (binding.noteTitle.text.toString().isNotEmpty()) {
                                    viewModel.updateCurrentNote(
                                        ObjectId(args.id),
                                        binding.noteTitle.text.toString(),
                                        binding.noteText.text.toString(),
                                        noteUIState.image,
                                        false
                                    )
                                }
                            }
                            if (noteUIState.image.isNotEmpty()) {
                                imageNoteAdapter.updateList(listOf(ImageNote(src = noteUIState.image)))
                                binding.noteDeleteImageButton.setOnClickListener {
                                    binding.noteImageRecyclerView.visibility = View.GONE
                                    binding.createNoteProgressIndicator.visibility = View.VISIBLE
                                    if (!noteUIState.image.contains("content")) {
                                        val toDeleteRef =
                                            storage.getReferenceFromUrl(noteUIState.image)
                                        viewModel.deleteImage(
                                            toDeleteRef,
                                            ObjectId(args.id),
                                            binding.noteTitle.text.toString(),
                                            binding.noteText.text.toString()
                                        )
                                    } else {
                                        viewModel.updateCurrentNote(
                                            ObjectId(args.id),
                                            binding.noteTitle.text.toString(),
                                            binding.noteText.text.toString(),
                                            "",
                                            true
                                        )
                                    }
                                    imageNoteAdapter.updateList(emptyList())
                                    binding.createNoteProgressIndicator.visibility = View.GONE

                                }

                                binding.noteDeleteImageButton.visibility = View.VISIBLE
                                binding.noteAddImageButton.visibility = View.GONE
                                binding.noteImageRecyclerView.visibility = View.VISIBLE
                            } else {
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteAddImageButton.setOnClickListener {
                                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                binding.noteDeleteImageButton.visibility = View.GONE
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteImageRecyclerView.visibility = View.GONE
                            }
                        } else {
                            binding.topAppBar.menu.findItem(R.id.delete_note_menu_option).isVisible =
                                false
                            binding.noteAddImageButton.setOnClickListener {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            if (noteUIState.image.isNotEmpty()) {
                                imageNoteAdapter.updateList(listOf(ImageNote(src = noteUIState.image)))
                                binding.noteDeleteImageButton.setOnClickListener {
                                    viewModel.updateCurrentState(
                                        binding.noteTitle.text.toString(),
                                        binding.noteText.text.toString(),
                                        Uri.EMPTY
                                    )
                                    isImagePickerOpened = false
                                    imageNoteAdapter.updateList(emptyList())

                                }
                                binding.noteDeleteImageButton.visibility = View.VISIBLE
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteImageRecyclerView.visibility = View.VISIBLE
                            } else {
                                binding.noteDeleteImageButton.visibility = View.GONE
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteImageRecyclerView.visibility = View.GONE
                            }
                            binding.noteSendButton.setOnClickListener {
                                viewModel.updateCurrentState(
                                    binding.noteTitle.text.toString(),
                                    binding.noteText.text.toString(),
                                    Uri.parse(noteUIState.image)
                                )
                                viewModel.createNote()
                            }
                        }
                    }
                }
                launch {
                    viewModel.isNoteJobDone.collectLatest {
                        if (it) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
        binding.noteImageRecyclerView.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageNoteAdapter
        }
        return binding.root
    }
}