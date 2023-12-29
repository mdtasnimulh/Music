package com.tasnim.chowdhury.music.adapters.diffUtil

import androidx.recyclerview.widget.DiffUtil
import com.tasnim.chowdhury.music.model.Music

class MusicCallBack(private val oldList: List<Music>, private val newList: List<Music>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition].id != newList[newItemPosition].id -> {
                false
            }
            oldList[oldItemPosition].title != newList[newItemPosition].title -> {
                false
            }
            else -> true
        }
    }
}