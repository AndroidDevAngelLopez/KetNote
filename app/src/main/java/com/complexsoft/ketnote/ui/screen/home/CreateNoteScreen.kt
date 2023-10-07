package com.complexsoft.ketnote.ui.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.databinding.CreateNoteDialogLayoutBinding
import org.mongodb.kbson.BsonObjectId

class CreateNoteScreen : DialogFragment(R.layout.create_note_dialog_layout) {
    private lateinit var binding: CreateNoteDialogLayoutBinding
    val args: CreateNoteScreenArgs by navArgs()

    override fun onStart() {
        super.onStart()
        setFullScreen()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = CreateNoteDialogLayoutBinding.inflate(layoutInflater)
        val viewModel by viewModels<HomeScreenViewModel>()
        if (args.id.isNotBlank()) {
            val note = viewModel.getNoteById(BsonObjectId(args.id))
            binding.sendNoteButton.text = "Update Note"
            binding.homeTitleNoteText.setText(note?.title)
            binding.homeTextNoteText.setText(note?.text)
            binding.sendNoteButton.setOnClickListener {
                viewModel.updateNote(
                    BsonObjectId(args.id),
                    binding.homeTitleNoteText.text.toString(),
                    binding.homeTextNoteText.text.toString()
                )
            }
        } else {
            binding.sendNoteButton.setOnClickListener {
                viewModel.insertNote(
                    binding.homeTitleNoteText.text.toString(),
                    binding.homeTextNoteText.text.toString()
                )
            }
        }

        return binding.root
    }

    private fun DialogFragment.setFullScreen() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}