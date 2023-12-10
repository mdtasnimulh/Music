package com.tasnim.chowdhury.music.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasnim.chowdhury.music.databinding.MusicListItemBinding

class MainAdapter(val context: Context): RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    private var mainList: MutableList<String> = mutableListOf("A", "B", "C", "D", "E")

    inner class MainViewHolder(private val binding: MusicListItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, list: String) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        return MainViewHolder(
            MusicListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return mainList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(position, mainList[position])
    }

    fun addAll(list: List<String>){
        mainList.addAll(list)
        notifyDataSetChanged()
    }

}