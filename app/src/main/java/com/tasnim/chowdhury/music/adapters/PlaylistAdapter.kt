package com.tasnim.chowdhury.music.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tasnim.chowdhury.music.databinding.PlaylistRvLayoutBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.ui.fragments.PlaylistFragment
import com.tasnim.chowdhury.music.utilities.Playlist

class PlaylistAdapter(): RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var playlistList: ArrayList<Playlist> = ArrayList()
    var playlistItem: ((position: Int, playlist: Playlist) -> Unit)? = null

    inner class PlaylistViewHolder(private val binding: PlaylistRvLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, playlist: Playlist) {
            Log.d("chkPlaylistItem", "$playlist")
            binding.playlistLayoutTitle.text = playlist.name
            binding.playlistLayoutCountText.text = playlist.createdBy
            binding.playlistLayoutCountNumber.text = "${playlist.playlist.size} Songs"

            binding.deletePlaylist.setOnClickListener {
                val cancelDialog = AlertDialog.Builder(it.context)
                    .setTitle(playlist.name)
                    .setMessage("Are you sure you want to delete this playlist?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        PlaylistFragment.musicPlaylist.ref.removeAt(position)
                        refreshPlaylist()
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                cancelDialog.create()
                cancelDialog.show()
            }

            binding.root.setOnClickListener {
                playlistItem?.invoke(position, playlist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        return PlaylistViewHolder(
            PlaylistRvLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(position, playlistList[position])
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    fun addPlaylist(playlistList: ArrayList<Playlist>) {
        playlistList.clear()
        playlistList.addAll(playlistList)
        notifyDataSetChanged()
    }

    fun refreshPlaylist() {
        playlistList = ArrayList()
        playlistList.addAll(PlaylistFragment.musicPlaylist.ref)
        notifyDataSetChanged()
    }

}