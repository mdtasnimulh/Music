package com.tasnim.chowdhury.music.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.MusicListItemBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.MainFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.ui.fragments.PlaylistDetailsFragment
import com.tasnim.chowdhury.music.ui.fragments.PlaylistFragment
import com.tasnim.chowdhury.music.utilities.formatDuration

class MusicAdapter(val context: Context, val playlistDetails: Boolean = false,
    val selectionFragment: Boolean = false) : RecyclerView.Adapter<MusicAdapter.MainViewHolder>() {

    private var musicList: ArrayList<Music> = arrayListOf()
    var musicItem: ((position: Int, tag: String, song: Music) -> Unit)? = null

    inner class MainViewHolder(private val binding: MusicListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, music: Music) {
            binding.songTitle.text = music.title
            binding.artistName.text = music.artist
            binding.songDuration.text = formatDuration(music.duration)
            Glide.with(context)
                .load(music.artUri)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                .into(binding.songImage)

            Log.d("chkMusicListSize", "Position::$position")

            if (selectionFragment) {
                binding.root.setOnClickListener {
                    if (addSongToPlaylist(music)) {
                        binding.musicListItem.backgroundTintList = ContextCompat.getColorStateList(context, R.color.palette1Maroon)
                    } else {
                        binding.musicListItem.backgroundTintList = null
                    }
                }
            } else {
                if (playlistDetails) {
                    binding.root.setOnClickListener {
                        musicItem?.invoke(
                            position,
                            "PlaylistDetails",
                            music
                        )
                    }
                } else {
                    binding.musicListItem.setOnClickListener {
                        if (music.id == PlayerFragment.nowPlayingId) {
                            musicItem?.invoke(
                                position,
                                "NowPlaying",
                                music
                            )
                        } else {
                            if (!MainFragment.search) {
                                musicItem?.invoke(
                                    position,
                                    "MainAdapter",
                                    music
                                )
                            } else {
                                musicItem?.invoke(
                                    position,
                                    "SearchView",
                                    music
                                )
                            }
                        }
                    }
                }
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

    fun addAll(list: List<Music>) {
        musicList.clear()
        musicList.addAll(list.distinctBy { it.id })
        Log.d("chkMusicListSize", "${list.size}")
        notifyDataSetChanged()
    }

    fun addSongToPlaylist(song: Music): Boolean {
        PlaylistFragment.musicPlaylist.ref[PlaylistDetailsFragment.currentPlaylistPosition].playlist
            .forEachIndexed { index, music ->
            if (song.id == music.id) {
                PlaylistFragment.musicPlaylist.ref[PlaylistDetailsFragment.currentPlaylistPosition].playlist.removeAt(index)
                return false
            }
        }
        PlaylistFragment.musicPlaylist.ref[PlaylistDetailsFragment.currentPlaylistPosition].playlist.add(song)
        return true
    }

    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList = PlaylistFragment.musicPlaylist.ref[PlaylistDetailsFragment.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }

}