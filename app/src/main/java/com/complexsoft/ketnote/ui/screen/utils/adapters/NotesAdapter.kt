package com.complexsoft.ketnote.ui.screen.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.Note
import com.google.android.material.textview.MaterialTextView
import toHumanDate

class NotesAdapter(private var notes: List<Note>, private val onNoteClick: (Note) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.MainNoteViewHolder>() {

    fun updateList(newNotes: List<Note>) {
        val noteDiff = NoteDiffUtil(notes, newNotes)
        val result = DiffUtil.calculateDiff(noteDiff)
        notes = newNotes
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainNoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.note_item_layout, parent, false)
        return MainNoteViewHolder(view)
    }

    class MainNoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: MaterialTextView
        val noteText: MaterialTextView
        val noteDate: MaterialTextView
        val noteImage: ImageView

        init {
            noteTitle = view.findViewById(R.id.note_item_title)
            noteText = view.findViewById(R.id.note_item_text)
            noteDate = view.findViewById(R.id.note_item_date)
            noteImage = view.findViewById(R.id.note_item_image)
        }
    }

    override fun onBindViewHolder(holder: MainNoteViewHolder, position: Int) {
        holder.noteTitle.text = notes[position].title
        if (holder.noteText.text.isNotBlank()) {
            holder.noteText.visibility = View.VISIBLE
            holder.noteText.text = notes[position].text
        } else {
            holder.noteText.visibility = View.GONE
        }
        holder.noteDate.text = notes[position].date.toHumanDate()
        if (notes[position].images.isNotBlank()) {
            holder.noteImage.visibility = View.VISIBLE
            Glide.with(holder.noteImage).load(notes[position].images).into(holder.noteImage)
        } else {
            holder.noteImage.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            onNoteClick(notes[position])
        }
    }

    override fun getItemCount(): Int = notes.size
}