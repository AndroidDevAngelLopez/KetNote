package com.complexsoft.ketnote.ui.screen.home

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.ImageNote
import com.complexsoft.ketnote.databinding.CreateNoteDialogLayoutBinding
import com.complexsoft.ketnote.ui.screen.components.createDialog
import com.complexsoft.ketnote.ui.screen.utils.adapters.ImageNoteAdapter
import com.complexsoft.ketnote.utils.toImageNoteList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import org.mongodb.kbson.ObjectId

@AndroidEntryPoint
class CreateNoteScreen : DialogFragment(R.layout.create_note_dialog_layout) {

    private lateinit var binding: CreateNoteDialogLayoutBinding
    private val args: CreateNoteScreenArgs by navArgs()
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var openChooser: ActivityResultLauncher<Intent>
    private lateinit var uploadTask: StorageReference
    private lateinit var _image: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var imageNoteAdapter: ImageNoteAdapter
    private lateinit var sendIntent: Intent
    private lateinit var shareIntent: Intent
    val viewModel by viewModels<HomeScreenViewModel>()
    private var flag = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = Firebase.storage
        storageRef = storage.reference
        openChooser = viewModel.openPhotoShareDialog(this)
        pickMedia = viewModel.openPhotoPicker(this) { images ->
            imageNoteAdapter.updateList(images.toImageNoteList())
            for (image in images) {
                _image = image
                val remoteImagePath =
                    "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${image.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                uploadTask = storageRef.child(remoteImagePath)
                flag = true
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreateNoteDialogLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        if (args.id.isNotBlank()) {
            val note = viewModel.getNoteById(ObjectId(args.id))
            binding.noteDialogShare.visibility = View.VISIBLE
            binding.noteDialogTitle.text = "Update Note"
            binding.deleteNoteButton.visibility = View.VISIBLE
            imageNoteAdapter = ImageNoteAdapter(listOf(ImageNote(src = note!!.images))) {
                createDialog(
                    this.context,
                    "image clicked",
                    "this image has been clicked",
                    "dismiss",
                    "action"
                ) {
                    Log.d("image Clicked", "omg clicked")
                }?.show()

            }
            imageNoteAdapter.updateList(listOf(ImageNote(src = note.images)))
            binding.sendNoteButton.text = "Update Note"
            binding.homeTitleNoteText.setText(note.title)
            binding.homeTextNoteText.setText(note.text)
            binding.addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.noteDialogShare.setOnClickListener {
                val bitmap = viewModel.generateJpegImage(note, this)
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, bitmap)
                    type = "image/jpeg"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Share with")
//                openChooser.launch(shareIntent)
                requireContext().startActivity(shareIntent)
            }
            binding.sendNoteButton.setOnClickListener {
                if (flag) {
                    val toDeleteRef = storage.getReferenceFromUrl(note.images)
                    viewModel.deletePhotoFromFirebase(toDeleteRef) {
                        viewModel.uploadPhotoToFirebase(uploadTask, _image) {
                            viewModel.updateNote(
                                note._id,
                                binding.homeTitleNoteText.text.toString(),
                                binding.homeTextNoteText.text.toString(),
                                it
                            )
                            this.dismiss()
                        }
                    }
                } else {
                    viewModel.updateNote(
                        note._id,
                        binding.homeTitleNoteText.text.toString(),
                        binding.homeTextNoteText.text.toString(),
                        note.images
                    )
                    this.dismiss()
                }
            }
            binding.deleteNoteButton.setOnClickListener {
                if (note.images.isNotEmpty()) {
                    val toDeleteRef = storage.getReferenceFromUrl(note.images)
                    viewModel.deletePhotoFromFirebase(toDeleteRef) {
                        viewModel.deleteNoteById(note._id)
                        this.dismiss()
                    }
                } else {
                    viewModel.deleteNoteById(note._id)
                    this.dismiss()
                }
            }
        } else {
            binding.noteDialogShare.visibility = View.GONE
            binding.noteDialogTitle.text = "Create Note"
            imageNoteAdapter = ImageNoteAdapter(emptyList()) {

            }
            binding.deleteNoteButton.visibility = View.GONE
            binding.addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.sendNoteButton.setOnClickListener {
                if (flag) {
                    viewModel.uploadPhotoToFirebase(uploadTask, _image) {
                        viewModel.insertNote(
                            binding.homeTitleNoteText.text.toString(),
                            binding.homeTextNoteText.text.toString(),
                            it
                        )
                        this.dismiss()
                    }
                } else {
                    viewModel.insertNote(
                        binding.homeTitleNoteText.text.toString(),
                        binding.homeTextNoteText.text.toString(),
                        ""
                    )
                    this.dismiss()
                }
            }
        }
        binding.imageReceivedRecycler.apply {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = imageNoteAdapter
        }
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}