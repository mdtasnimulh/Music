package com.tasnim.chowdhury.music.adapters

import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.PlaylistRvLayoutBinding
import com.tasnim.chowdhury.music.ui.fragments.playlist.PlaylistFragment
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

            if (playlist.playlist.size > 0) {
                /*val imageArt = getImageArt(playlist.playlist[0].path)
                val image = if (imageArt != null) {
                    BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
                } else {
                    BitmapFactory.decodeResource(itemView.context.resources, R.drawable.ic_launcher_foreground)
                }*/
                Glide.with(itemView.context)
                    .load(playlist.playlist[0].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher).centerCrop())
                    .into(binding.playlistAlbumMainImage)

                Glide.with(itemView.context)
                    .load(playlist.playlist[0].artUri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher).centerCrop())
                    .into(binding.playlistAlbumSubImage)

                // Rotate the playlistAlbumSubImage
                val rotation = ObjectAnimator.ofFloat(binding.playlistAlbumSubImage, "rotation", 0f, 360f)
                rotation.repeatCount = ObjectAnimator.INFINITE
                rotation.interpolator = LinearInterpolator()
                rotation.duration = 4000 // Set the rotation duration in milliseconds (adjust as needed)
                rotation.start()
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
        this.playlistList.addAll(playlistList)
        notifyDataSetChanged()
    }

    fun refreshPlaylist() {
        playlistList = ArrayList()
        playlistList.addAll(PlaylistFragment.musicPlaylist.ref)
        notifyDataSetChanged()
    }

}