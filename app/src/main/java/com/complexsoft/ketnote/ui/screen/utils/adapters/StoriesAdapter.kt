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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

class StoriesAdapter(private var notes: List<Note>, private val onNoteClick: (Note) -> Unit) :
    RecyclerView.Adapter<StoriesAdapter.NoteViewHolder>() {

    fun updateList(newNotes: List<Note>) {
        val noteDiff = NoteDiffUtil(notes, newNotes)
        val result = DiffUtil.calculateDiff(noteDiff)
        notes = newNotes
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.home_screen_header, parent, false)
        return NoteViewHolder(view)
    }

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: MaterialTextView
        val noteImage: ImageView
        val noteCard: MaterialCardView

        init {
            noteCard = view.findViewById(R.id.carousel_note_item)
            noteTitle = view.findViewById(R.id.stories_item_title)
            noteImage = view.findViewById(R.id.stories_item_image)
        }
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.noteTitle.text = notes[position].title
        if (notes[position].images.isNotBlank()) {
            holder.noteImage.visibility = View.VISIBLE
            Glide.with(holder.noteImage).load(notes[position].images).into(holder.noteImage)
        } else {
            holder.noteImage.visibility = View.GONE
        }
        holder.noteCard.setOnClickListener {
            onNoteClick(notes[position])
        }
    }

    override fun getItemCount(): Int = notes.size
}