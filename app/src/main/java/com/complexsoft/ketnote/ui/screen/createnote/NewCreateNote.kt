package com.complexsoft.ketnote.ui.screen.createnote

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.complexsoft.ketnote.databinding.NewCreateFragmentLayoutBinding
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
class NewCreateNote : Fragment(R.layout.new_create_fragment_layout) {
    private lateinit var uploadTask: StorageReference
    private lateinit var _image: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var imageNoteAdapter: ImageNoteAdapter
    private lateinit var binding: NewCreateFragmentLayoutBinding
    private val viewModel by viewModels<CreateNoteViewModel>()
    private val args: NewCreateNoteArgs by navArgs()
    private var isImagePickerOpened = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        storage = Firebase.storage
        storageRef = storage.reference
        binding = NewCreateFragmentLayoutBinding.inflate(layoutInflater)
        imageNoteAdapter = ImageNoteAdapter(emptyList()) {
            if (it.src.isNotEmpty()) {
                val action = NewCreateNoteDirections.actionNewCreateNoteToImageVisorFragment(it.src)
                findNavController().navigate(action)
            }
        }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uri ->
                if (uri != null) {
                    if (uri.isNotEmpty()) {
                        viewModel.updateCurrentState(
                            binding.noteTitle.text.toString(),
                            binding.noteText.text.toString(),
                            uri[0]
                        )
                        imageNoteAdapter.updateList(listOf(ImageNote(src = uri[0].toString())))
                        val remoteImagePath =
                            "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${uri[0].lastPathSegment}-${System.currentTimeMillis()}.jpg"
                        uploadTask = storageRef.child(remoteImagePath)
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
                            binding.noteAddImageButton.setOnClickListener {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            binding.noteDeleteButton.visibility = View.VISIBLE
                            binding.noteSendButton.text = "Actualizar Nota"
                            binding.titleLabel.text = "Actualizar Nota"
                            binding.noteAddImageButton.icon = context?.let { context ->
                                ContextCompat.getDrawable(
                                    context, R.drawable.baseline_edit_24
                                )
                            }
                            binding.noteSendButton.visibility = View.VISIBLE
                            binding.noteTitle.setText(noteUIState.title)
                            binding.noteText.setText(noteUIState.text)
                            if (noteUIState.image.isNotEmpty()) {
                                imageNoteAdapter.updateList(listOf(ImageNote(src = noteUIState.image)))
                                binding.noteDeleteImageButton.setOnClickListener {
                                    Log.d("regrence url", noteUIState.image)
                                    val toDeleteRef = storage.getReferenceFromUrl(noteUIState.image)
                                    viewModel.deletePhotoFromFirebase(toDeleteRef) {
                                        viewModel.updateCurrentState(
                                            binding.noteTitle.text.toString(),
                                            binding.noteText.text.toString(),
                                            Uri.EMPTY
                                        )
                                        imageNoteAdapter.updateList(emptyList())
                                    }
                                }

                                binding.noteDeleteImageButton.visibility = View.VISIBLE
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteImageRecyclerView.visibility = View.VISIBLE
                            } else {
                                binding.noteAddImageButton.icon = context?.let { context ->
                                    ContextCompat.getDrawable(
                                        context, R.drawable.baseline_add_photo_alternate_24
                                    )
                                }
                                binding.noteDeleteImageButton.visibility = View.GONE
                                binding.noteAddImageButton.visibility = View.VISIBLE
                                binding.noteImageRecyclerView.visibility = View.GONE
                            }
                        } else {
                            binding.noteDeleteButton.visibility = View.GONE
                            binding.noteAddImageButton.setOnClickListener {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            binding.noteAddImageButton.icon = context?.let { context ->
                                ContextCompat.getDrawable(
                                    context, R.drawable.baseline_add_photo_alternate_24
                                )
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
                                binding.noteAddImageButton.icon = context?.let { context ->
                                    ContextCompat.getDrawable(
                                        context, R.drawable.baseline_add_photo_alternate_24
                                    )
                                }
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
                                if (!isImagePickerOpened) {
                                    uploadTask = storageRef.child("images/")
                                }
                                viewModel.insertNewNote(
                                    uploadTask
                                )
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