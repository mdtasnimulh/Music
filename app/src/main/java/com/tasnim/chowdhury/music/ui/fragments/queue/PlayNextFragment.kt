package com.tasnim.chowdhury.music.ui.fragments.queue

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.tasnim.chowdhury.music.adapters.FavouriteAdapter
import com.tasnim.chowdhury.music.databinding.FragmentPlayNextBinding
import com.tasnim.chowdhury.music.model.MusicList

class PlayNextFragment : Fragment() {

    private var _binding: FragmentPlayNextBinding? = null
    private val binding get() = _binding!!
    private lateinit var favouriteAdapter: FavouriteAdapter

    companion object {
        var playNextList = MusicList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayNextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        setupAdapter()
        setupClicks()
    }

    private fun initData() {
        if(playNextList.isNotEmpty()) {
            binding.instructionPN.visibility = View.GONE
        } else {
            binding.instructionPN.visibility = View.VISIBLE
        }
    }

    private fun setupAdapter() {
        favouriteAdapter = FavouriteAdapter("PlayNext")
        binding.playNextRv.adapter = favouriteAdapter
        binding.playNextRv.setHasFixedSize(true)
        binding.playNextRv.itemAnimator = DefaultItemAnimator()
        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.playNextRv.layoutManager = layoutManager

        Log.d("chkPlayNextList", "$playNextList")

        favouriteAdapter.addAllFavourites(playNextList)
    }

    private fun setupClicks() {
        favouriteAdapter.musicItem = { position, tag, _ ->
            when(tag) {
                "PlayNextAdapter" -> {
                    val action = PlayNextFragmentDirections.actionPlayNextFragmentToPlayerFragment(position, "PlayNextAdapter", playNextList)
                    findNavController().navigate(action)
                }
            }
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}