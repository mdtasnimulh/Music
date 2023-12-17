package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.FavouriteAdapter
import com.tasnim.chowdhury.music.databinding.FragmentFavouritesBinding

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favouriteAdapter: FavouriteAdapter

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
    }

    private fun setupAdapter() {
        favouriteAdapter = FavouriteAdapter()
        binding.favouriteRv.adapter = favouriteAdapter
        binding.favouriteRv.setHasFixedSize(true)
        binding.favouriteRv.itemAnimator = DefaultItemAnimator()
        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.favouriteRv.layoutManager = layoutManager

        val list: ArrayList<String> = arrayListOf(
            "asfasdf", "sdfas", "asdfasdfas",
            "afasfda", "afasfdasdf", "asfasdf",
            "afasfda", "sdfas", "asfasdf",
            "sdfas", "asfasdf"
        )
        favouriteAdapter.addAllFavourites(list)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}