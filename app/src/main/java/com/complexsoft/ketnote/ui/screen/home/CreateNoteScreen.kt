package com.complexsoft.ketnote.ui.screen.home

import android.app.AlertDialog
import android.app.Dialog
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
import com.complexsoft.ketnote.ui.screen.utils.adapters.ImageNoteAdapter
import com.complexsoft.ketnote.utils.toImageNoteList
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.mongodb.kbson.ObjectId

class CreateNoteScreen : DialogFragment(R.layout.create_note_dialog_layout) {
    private lateinit var binding: CreateNoteDialogLayoutBinding
    private val args: CreateNoteScreenArgs by navArgs()
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var uploadTask: StorageReference
    private lateinit var _image: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var imageNoteAdapter: ImageNoteAdapter
    val viewModel by viewModels<HomeScreenViewModel>()
    private var flag = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storage = Firebase.storage
        storageRef = storage.reference
        pickMedia = this.registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { images ->
            if (images != null) {
                imageNoteAdapter.updateList(images.toImageNoteList())
                for (image in images) {
                    _image = image
                    val remoteImagePath =
                        "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${image.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                    uploadTask = storageRef.child(remoteImagePath)
                    flag = true
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreateNoteDialogLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        if (args.id.isNotBlank()) {
            binding.noteDialogTitle.text = "Update Note"
            binding.deleteNoteButton.visibility = View.VISIBLE
            val note = viewModel.getNoteById(ObjectId(args.id))
            imageNoteAdapter = ImageNoteAdapter(listOf(ImageNote(src = note!!.images))) {
                context?.let {
                    MaterialAlertDialogBuilder(it)
                        .setTitle("Selected Photo")
                        .setMessage("All notes will be deleted !")
                        .setNeutralButton("Cancel") { dialog, which ->
                            // Respond to neutral button press
                            dialog.dismiss()
                        }.setPositiveButton("delete notes") { dialog, which ->
                            viewModel.deleteAllNotes()
                        }.show()
                }
            }
            imageNoteAdapter.updateList(listOf(ImageNote(src = note.images)))
            binding.sendNoteButton.text = "Update Note"
            binding.homeTitleNoteText.setText(note.title)
            binding.homeTextNoteText.setText(note.text)
            binding.addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.sendNoteButton.setOnClickListener {
                if (flag) {
                    uploadTask.putFile(_image).addOnFailureListener {
                        Log.d("failure in upload image", it.message.toString())
                    }.addOnSuccessListener { taskSnapshot ->
                        uploadTask.downloadUrl.addOnSuccessListener {
                            viewModel.updateNote(
                                ObjectId(args.id),
                                binding.homeTitleNoteText.text.toString(),
                                binding.homeTextNoteText.text.toString(),
                                it.toString()
                            )
                            this.dismiss()
                        }
                    }
                } else {
                    viewModel.updateNote(
                        ObjectId(args.id),
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
                    toDeleteRef.delete().addOnSuccessListener {
                        viewModel.deleteNoteById(ObjectId(args.id))
                        this.dismiss()
                    }
                } else {
                    viewModel.deleteNoteById(ObjectId(args.id))
                    this.dismiss()
                }
            }
        } else {
            binding.noteDialogTitle.text = "Create Note"
            imageNoteAdapter = ImageNoteAdapter(emptyList()) {

            }
            binding.deleteNoteButton.visibility = View.GONE
            binding.addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.sendNoteButton.setOnClickListener {
                if (flag) {
                    uploadTask.putFile(_image).addOnFailureListener {
                        Log.d("failure in upload image", it.message.toString())
                    }.addOnSuccessListener { taskSnapshot ->
                        uploadTask.downloadUrl.addOnSuccessListener { uri ->
                            viewModel.insertNote(
                                binding.homeTitleNoteText.text.toString(),
                                binding.homeTextNoteText.text.toString(),
                                uri.toString()
                            )
                            this.dismiss()
                        }
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