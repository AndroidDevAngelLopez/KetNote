package com.complexsoft.ketnote.ui.screen.home

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.CreateNoteDialogLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.mongodb.kbson.ObjectId

class CreateNoteScreen : DialogFragment(R.layout.create_note_dialog_layout) {
    private lateinit var binding: CreateNoteDialogLayoutBinding
    private val args: CreateNoteScreenArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreateNoteDialogLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        val storage = Firebase.storage
        val storageRef = storage.reference

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { image ->
                if (image != null) {
                    val remoteImagePath =
                        "images/${FirebaseAuth.getInstance().currentUser?.uid}/" + "${image.lastPathSegment}-${System.currentTimeMillis()}.jpg"
                    val uploadTask = storageRef.child(remoteImagePath)
                    uploadTask.putFile(image).addOnFailureListener {
                        Log.d("failure in upload image", it.message.toString())
                    }.addOnSuccessListener { taskSnapshot ->
                        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                        // ...
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected")
                }
            }
        val viewModel by viewModels<HomeScreenViewModel>()
        if (args.id.isNotBlank()) {
            binding.deleteNoteButton.visibility = View.VISIBLE
            val note = viewModel.getNoteById(ObjectId(args.id))
            binding.sendNoteButton.text = "Update Note"
            binding.homeTitleNoteText.setText(note?.title)
            binding.homeTextNoteText.setText(note?.text)
            binding.sendNoteButton.setOnClickListener {
                viewModel.updateNote(
                    ObjectId(args.id),
                    binding.homeTitleNoteText.text.toString(),
                    binding.homeTextNoteText.text.toString()
                )
                this.dismiss()
            }
            binding.deleteNoteButton.setOnClickListener {
                viewModel.deleteNoteById(ObjectId(args.id))
                this.dismiss()
            }
        } else {
            binding.deleteNoteButton.visibility = View.GONE
            binding.addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            binding.sendNoteButton.setOnClickListener {
                viewModel.insertNote(
                    binding.homeTitleNoteText.text.toString(),
                    binding.homeTextNoteText.text.toString()
                )
                this.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
}