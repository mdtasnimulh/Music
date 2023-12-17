package com.tasnim.chowdhury.music.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tasnim.chowdhury.music.databinding.FavouriteItemBinding

class FavouriteAdapter: RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    private val favouriteList: ArrayList<String> = arrayListOf()

    inner class FavouriteViewHolder(val binding: FavouriteItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int, s: String) {
            binding.favouriteItemTitle.text = s
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

    fun addAllFavourites(list: List<String>) {
        favouriteList.clear()
        favouriteList.addAll(list)
        notifyDataSetChanged()
    }
}