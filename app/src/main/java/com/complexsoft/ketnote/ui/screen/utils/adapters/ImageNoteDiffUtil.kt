package com.complexsoft.ketnote.ui.screen.utils.adapters

import androidx.recyclerview.widget.DiffUtil
import com.complexsoft.ketnote.data.model.ImageNote

class ImageNoteDiffUtil(
    private val oldList: List<ImageNote>, private val newList: List<ImageNote>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList.size == newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].src != newList[newItemPosition].src -> {
                false
            }

            else -> {
                true
            }
        }
    }
}