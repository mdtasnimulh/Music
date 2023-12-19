package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentPlaylistDetailsBinding

class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<PlaylistDetailsFragmentArgs>()
    private lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPosition: Int = -1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentPlaylistPosition = args.position
        setupAdapter()
    }

    private fun setupAdapter() {
        adapter = MusicAdapter(requireContext(), true)
        binding.playlistRV.adapter = adapter
        binding.playlistRV.setItemViewCacheSize(10)
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.layoutManager = LinearLayoutManager(requireContext())
        adapter.addAll(PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].playlist)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}