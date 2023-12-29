package com.tasnim.chowdhury.music.adapters

import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FavouriteItemBinding
import com.tasnim.chowdhury.music.databinding.LongPressLayoutBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.queue.PlayNextFragment
import com.tasnim.chowdhury.music.ui.fragments.player.PlayerFragment
import com.tasnim.chowdhury.music.utilities.getImageArt

class FavouriteAdapter(val tag: String): RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    private val favouriteList: ArrayList<Music> = arrayListOf()
    var musicItem: ((position: Int, tag: String, song: Music) -> Unit)? = null

    inner class FavouriteViewHolder(val binding: FavouriteItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, music: Music) {
            binding.favouriteItemTitle.text = music.title
            /*val imageArt = getImageArt(music.path)
            val image = if (imageArt != null) {
                BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
            } else {
                BitmapFactory.decodeResource(itemView.context.resources, R.drawable.ic_launcher_foreground)
            }*/
            Glide.with(itemView.context)
                .load(music.artUri)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                .into(binding.favouriteItemCoverImage)

            when(tag) {
                "Favourite" -> {
                    binding.root.setOnClickListener {
                        musicItem?.invoke(
                            position,
                            "FavouriteAdapter",
                            music
                        )
                    }
                }
                "PlayNext" -> {
                    binding.root.setOnClickListener {
                        musicItem?.invoke(
                            position,
                            "PlayNextAdapter",
                            music
                        )
                    }

                    binding.root.setOnLongClickListener {
                        val customDialog = LayoutInflater.from(itemView.context).inflate(R.layout.long_press_layout, null)
                        val bindingMF = LongPressLayoutBinding.bind(customDialog)
                        val dialog = MaterialAlertDialogBuilder(itemView.context).setView(customDialog)
                            .create()
                        dialog.show()
                        dialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
                        bindingMF.addToNextBtn.text = "Remove"
                        bindingMF.addToNextBtn.setOnClickListener {
                            if (position == PlayerFragment.songPosition){
                                Snackbar.make(binding.root, "Can't remove currently playing song", Snackbar.LENGTH_SHORT).show()
                            } else {
                                PlayNextFragment.playNextList.removeAt(position)
                                PlayerFragment.musicList?.removeAt(position)
                                notifyItemRemoved(position)
                                refreshPlayNext()
                            }
                            dialog.dismiss()
                        }
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        return FavouriteViewHolder(
            FavouriteItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.bind(position, favouriteList[position])
    }

    override fun getItemCount(): Int {
        return favouriteList.size
    }

    fun addAllFavourites(list: List<Music>) {
        favouriteList.clear()
        favouriteList.addAll(list)
        notifyDataSetChanged()
    }

    fun refreshPlayNext() {
        favouriteList.clear()
        favouriteList.addAll(PlayNextFragment.playNextList)
        notifyDataSetChanged()
    }
}