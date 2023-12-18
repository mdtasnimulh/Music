package com.tasnim.chowdhury.music.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FavouriteItemBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment

class FavouriteAdapter: RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    private val favouriteList: ArrayList<Music> = arrayListOf()
    var musicItem: ((position: Int, tag: String, song: Music) -> Unit)? = null

    inner class FavouriteViewHolder(val binding: FavouriteItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, music: Music) {
            binding.favouriteItemTitle.text = music.title
            Glide.with(itemView.context)
                .load(music.artUri)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                .into(binding.favouriteItemCoverImage)

            binding.root.setOnClickListener {
                musicItem?.invoke(
                    position,
                    "FavouriteAdapter",
                    music
                )
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
}