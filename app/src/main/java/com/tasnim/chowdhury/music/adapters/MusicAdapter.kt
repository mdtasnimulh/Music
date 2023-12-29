package com.tasnim.chowdhury.music.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.diffUtil.MusicCallBack
import com.tasnim.chowdhury.music.databinding.DetailsViewBinding
import com.tasnim.chowdhury.music.databinding.MusicListItemBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.ui.fragments.home.MainFragment
import com.tasnim.chowdhury.music.ui.fragments.queue.PlayNextFragment
import com.tasnim.chowdhury.music.ui.fragments.player.PlayerFragment
import com.tasnim.chowdhury.music.ui.fragments.playlist.PlaylistDetailsFragment
import com.tasnim.chowdhury.music.ui.fragments.playlist.PlaylistFragment
import com.tasnim.chowdhury.music.utilities.formatDuration
import com.tasnim.chowdhury.music.utilities.getImageArt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

            /*GlobalScope.launch(Dispatchers.IO) {
                val imageArt = getImageArt(music.path)
                val image = if (imageArt != null) {
                    BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
                } else {
                    BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)
                }

                launch(Dispatchers.Main) {
                    Glide.with(context)
                        .load(image)
                        .apply(RequestOptions().override(70, 75))
                        .centerCrop().skipMemoryCache(false)
                        .into(binding.songImage)
                }
            }*/
            Glide.with(context)
                .load(music.artUri)
                .apply(RequestOptions().override(70, 75))
                .centerCrop().skipMemoryCache(false)
                .into(binding.songImage)

            Log.d("chkMusicListSize", "Position::$position")

            if (selectionFragment) {
                binding.root.setOnClickListener {
                    if (addSongToPlaylist(music)) {
                        binding.musicListItem.setCardBackgroundColor(ContextCompat.getColorStateList(context, R.color.palette1Grey))
                    } else {
                        binding.musicListItem.setCardBackgroundColor(ContextCompat.getColorStateList(context, R.color.white))
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

                    // long press
                    binding.musicListItem.setOnLongClickListener {
                        val view = LayoutInflater.from(context).inflate(R.layout.long_press_layout, null)
                        val createDialog = MaterialAlertDialogBuilder(context).setView(view)
                            .create()
                        createDialog.show()

                        val addToNextBtn = view.findViewById<Button>(R.id.addToNextBtn)
                        val songInfoBtn = view.findViewById<Button>(R.id.songInfoBtn)

                        addToNextBtn.setOnClickListener {
                            try {
                                if (PlayNextFragment.playNextList.isEmpty()) {
                                    PlayNextFragment.playNextList.add(
                                        PlayerFragment.musicList?.get(
                                            PlayerFragment.songPosition)!!)
                                    PlayerFragment.songPosition = 0
                                }
                                PlayNextFragment.playNextList.add(music)
                                PlayerFragment.musicList = MusicList()
                                PlayerFragment.musicList?.addAll(PlayNextFragment.playNextList)
                            }catch (e: Exception){
                                Log.d("CatchException", "${e.message}")
                            }
                            createDialog.dismiss()
                        }

                        songInfoBtn.setOnClickListener {
                            createDialog.dismiss()
                            val detailsDialog = LayoutInflater.from(context).inflate(R.layout.details_view, null)
                            val binder = DetailsViewBinding.bind(detailsDialog)
                            binder.detailsTV.setTextColor(Color.WHITE)
                            binder.root.setBackgroundColor(Color.TRANSPARENT)
                            val dDialog = MaterialAlertDialogBuilder(context)
                                .setView(detailsDialog)
                                .setPositiveButton("OK"){self, _ -> self.dismiss()}
                                .setCancelable(false)
                                .create()
                            dDialog.show()
                            dDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                            dDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                            val str = SpannableStringBuilder().bold { append("DETAILS\n\nName: ") }
                                .append(music.title)
                                .bold { append("\n\nDuration: ") }.append(DateUtils.formatElapsedTime(music.duration/1000))
                                .bold { append("\n\nLocation: ") }.append(music.path)
                            binder.detailsTV.text = str
                        }

                        return@setOnLongClickListener true
                    }
                }
            }

            if (music.id == PlayerFragment.nowPlayingId) {
                binding.musicListItem.setCardBackgroundColor(Color.parseColor("#33757575"))
            } else {
                binding.musicListItem.setCardBackgroundColor(Color.parseColor("#00FFFFFF"))
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

    fun setMusic(newMusic: List<Music>) {
        val diffCallBack = MusicCallBack(newMusic, newMusic)
        val diffMusic = DiffUtil.calculateDiff(diffCallBack)
        musicList.clear()
        musicList.addAll(newMusic)
        diffMusic.dispatchUpdatesTo(this)
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