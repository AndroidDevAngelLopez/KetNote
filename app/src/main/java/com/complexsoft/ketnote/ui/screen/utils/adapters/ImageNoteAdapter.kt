package com.complexsoft.ketnote.ui.screen.utils.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.complexsoft.ketnote.R
import com.complexsoft.ketnote.data.model.ImageNote

class ImageNoteAdapter(
    private var images: List<ImageNote>, private val onImageNoteClick: (ImageNote) -> Unit
) : RecyclerView.Adapter<ImageNoteAdapter.ImageNoteViewHolder>() {

    fun updateList(newImages: List<ImageNote>) {
        val imageDiff = ImageNoteDiffUtil(images, newImages)
        val result = DiffUtil.calculateDiff(imageDiff)
        images = newImages
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_note_item_layout, parent, false)
        return ImageNoteViewHolder(view)
    }

    class ImageNoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView

        init {
            image = view.findViewById(R.id.image_note_item_image)
        }
    }

    override fun onBindViewHolder(holder: ImageNoteViewHolder, position: Int) {
        Glide.with(holder.image).load(images[position].src).into(holder.image)
        holder.itemView.setOnClickListener {
            onImageNoteClick(images[position])
        }
    }

    override fun getItemCount(): Int = images.size
}