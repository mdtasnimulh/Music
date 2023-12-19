package com.tasnim.chowdhury.music.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.GsonBuilder
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentPlaylistDetailsBinding
import com.tasnim.chowdhury.music.model.MusicList

class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<PlaylistDetailsFragmentArgs>()
    private lateinit var adapter: MusicAdapter
    var playlistDetailsSongs: MusicList = MusicList()
    private var shuffledMusicList: MusicList = MusicList()

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
        setupClicks()
    }

    override fun onResume() {
        super.onResume()

        binding.playlistName.text = PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].name
        binding.totalSongsTv.text = "Total Songs: ${adapter.itemCount}"
        binding.createdByTv.text = "Created by: ${PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].createdBy}"
        binding.createdOnTv.text = "Created on: ${PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].createdOn}"

        if (adapter.itemCount > 0) {
            Glide.with(requireContext())
                .load(PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                .into(binding.playlistCoverImage)
            binding.shufflePlaylistBtn.visibility = View.VISIBLE
        } else {
            binding.shufflePlaylistBtn.visibility = View.GONE
        }

        adapter.notifyDataSetChanged()

        val editorPlaylist = activity?.getSharedPreferences("PLAYLIST", Context.MODE_PRIVATE)?.edit()
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistFragment.musicPlaylist)
        editorPlaylist?.putString("MusicPlaylist", jsonStringPlaylist)
        editorPlaylist?.apply()
    }

    private fun setupAdapter() {
        adapter = MusicAdapter(requireContext(), true)
        binding.playlistRV.adapter = adapter
        binding.playlistRV.setItemViewCacheSize(10)
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.layoutManager = LinearLayoutManager(requireContext())
        adapter.addAll(PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].playlist)
    }

    private fun setupClicks() {
        playlistDetailsSongs.addAll(PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].playlist)
        adapter.musicItem = { position, tag, song ->
            when(tag) {
                "PlaylistDetails" -> {
                    val action = PlaylistDetailsFragmentDirections.actionPlaylistDetailsFragmentToPlayerFragment(position, "PlaylistDetails", playlistDetailsSongs)
                    findNavController().navigate(action)
                }
            }
        }

        binding.playerBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.shufflePlaylistBtn.setOnClickListener {
            shuffledMusicList.clear()
            shuffledMusicList.addAll(playlistDetailsSongs)
            shuffledMusicList.shuffle()
            val shuffleAction = PlaylistDetailsFragmentDirections.actionPlaylistDetailsFragmentToPlayerFragment(0, "ShufflePlaylistSongs", shuffledMusicList)
            findNavController().navigate(shuffleAction)
        }

        binding.addSongBtn.setOnClickListener {
            findNavController().navigate(R.id.action_playlistDetailsFragment_to_selectSongsFragment2)
        }

        binding.removeAllSongsBtn.setOnClickListener {
            val cancelDialog = AlertDialog.Builder(requireContext())
                .setTitle("Remove All Songs")
                .setMessage("Are you sure you want to remove all songs from the list?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistFragment.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            cancelDialog.create()
            cancelDialog.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}