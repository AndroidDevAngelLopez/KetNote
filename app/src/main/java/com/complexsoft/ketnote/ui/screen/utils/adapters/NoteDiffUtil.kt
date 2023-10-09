package com.complexsoft.ketnote.ui.screen.utils.adapters

import androidx.recyclerview.widget.DiffUtil
import com.complexsoft.ketnote.data.model.Note

class NoteDiffUtil(
    private val oldList: List<Note>, private val newList: List<Note>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldList.size == newList.size){
            oldList[oldItemPosition]._id == newList[newItemPosition]._id
        }else{
            false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].title != newList[newItemPosition].title -> {
                false
            }

            oldList[oldItemPosition].text != newList[newItemPosition].text -> {
                false
            }

            oldList[oldItemPosition].date != newList[newItemPosition].date -> {
                false
            }
            oldList[oldItemPosition].owner_id != newList[newItemPosition].owner_id -> {
                false
            }

            else -> {
                true
            }
        }
    }
}