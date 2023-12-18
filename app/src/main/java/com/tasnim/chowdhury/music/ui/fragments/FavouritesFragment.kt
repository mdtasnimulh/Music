package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.FavouriteAdapter
import com.tasnim.chowdhury.music.databinding.FragmentFavouritesBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favouriteAdapter: FavouriteAdapter
    private val favouritesMusicList = MusicList()

    companion object {
        var favouriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupClicks()
    }

    private fun setupClicks() {
        favouritesMusicList.addAll(favouriteSongs)
        favouriteAdapter.musicItem = { position, tag, _ ->
            when(tag) {
                "FavouriteAdapter" -> {
                    val action = FavouritesFragmentDirections.actionFavouritesFragmentToPlayerFragment(position, "FavouriteAdapter", favouritesMusicList)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun setupAdapter() {
        favouriteAdapter = FavouriteAdapter()
        binding.favouriteRv.adapter = favouriteAdapter
        binding.favouriteRv.setHasFixedSize(true)
        binding.favouriteRv.itemAnimator = DefaultItemAnimator()
        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.favouriteRv.layoutManager = layoutManager

        favouriteAdapter.addAllFavourites(favouriteSongs)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}