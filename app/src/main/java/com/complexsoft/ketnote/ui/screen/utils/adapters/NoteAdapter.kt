package com.complexsoft.ketnote.ui.screen.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.Note
import com.complexsoft.ketnote.utils.toHumanDate
import com.google.android.material.textview.MaterialTextView

class NoteAdapter(private var notes: List<Note>, private val onNoteClick: (Note) -> Unit) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    fun updateList(newNotes: List<Note>) {
        val noteDiff = NoteDiffUtil(notes, newNotes)
        val result = DiffUtil.calculateDiff(noteDiff)
        notes = newNotes
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        return NoteViewHolder(view)
    }

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: MaterialTextView
        val noteText: MaterialTextView
        val noteDate: MaterialTextView

        init {
            noteTitle = view.findViewById(R.id.note_item_title)
            noteText = view.findViewById(R.id.note_item_text)
            noteDate = view.findViewById(R.id.note_item_date)
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.noteTitle.text = notes[position].title
        holder.noteText.text = notes[position].text
        holder.noteDate.text = notes[position].date.toHumanDate()
        holder.itemView.setOnClickListener {
            onNoteClick(notes[position])
        }
    }

    override fun getItemCount(): Int = notes.size
}