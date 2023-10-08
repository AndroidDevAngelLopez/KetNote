package com.complexsoft.ketnote.ui.screen.home

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.CreateNoteDialogLayoutBinding
import org.mongodb.kbson.ObjectId

class CreateNoteScreen : DialogFragment(R.layout.create_note_dialog_layout) {
    private lateinit var binding: CreateNoteDialogLayoutBinding
    private val args: CreateNoteScreenArgs by navArgs()
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CreateNoteDialogLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
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