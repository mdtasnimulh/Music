package com.tasnim.chowdhury.music.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.MusicListItemBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.utilities.formatDuration

class MusicAdapter(val context: Context): RecyclerView.Adapter<MusicAdapter.MainViewHolder>() {

    private var musicList: List<Music> = listOf()
    var musicItem: ((position: Int, tag: String, song: Music) -> Unit)? = null

    inner class MainViewHolder(private val binding: MusicListItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, music: Music) {
            binding.songTitle.text = music.title
            binding.artistName.text = music.artist
            binding.songDuration.text = formatDuration(music.duration)
            Glide.with(context)
                .load(music.artUri)
                .apply ( RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop() )
                .into(binding.songImage)

            binding.musicListItem.setOnClickListener {
                musicItem?.invoke(
                    position,
                    "MainAdapter",
                    music
                )
            }
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
        return musicList.size
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.bind(position, musicList[position])
    }

    fun addAll(list: List<Music>){
        musicList = list
        notifyDataSetChanged()
    }

}