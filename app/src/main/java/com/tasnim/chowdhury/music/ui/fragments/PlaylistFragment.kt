package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.PlaylistAdapter
import com.tasnim.chowdhury.music.databinding.FragmentPlaylistBinding
import com.tasnim.chowdhury.music.utilities.MusicPlaylist
import com.tasnim.chowdhury.music.utilities.Playlist
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object{
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupClicks()
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }

    private fun setupAdapter() {
        playlistAdapter = PlaylistAdapter()
        binding.playlistRV.adapter = playlistAdapter
        binding.playlistRV.layoutManager = LinearLayoutManager(requireContext())
        playlistAdapter.addPlaylist(musicPlaylist.ref)
        Log.d("chkPlaylist", "${musicPlaylist.ref.size}")
    }

    private fun setupClicks() {
        binding.createPlaylistBtn.setOnClickListener {
            createPlaylistDialog()
        }

        playlistAdapter.playlistItem = { position, playlist ->
            val action = PlaylistFragmentDirections.actionPlaylistFragmentToPlaylistDetailsFragment(position)
            findNavController().navigate(action)
        }

        binding.playerBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun createPlaylistDialog() {
        val view = layoutInflater.inflate(R.layout.playlist_add_dialog, null)
        val createDialog = AlertDialog.Builder(requireContext()).create()
        createDialog.setView(view)

        createDialog.show()

        val playlistNameEt = view.findViewById<TextInputEditText>(R.id.playlistNameEt)
        val userNameEt = view.findViewById<TextInputEditText>(R.id.userNameEt)

        val playlistName = playlistNameEt.text
        val createdBy = userNameEt.text

        val addBtn = view.findViewById<AppCompatTextView>(R.id.addBtn)

        addBtn?.setOnClickListener {
            if (playlistName.toString().isNotEmpty() && createdBy.toString().isNotEmpty()){
                addPlaylist(playlistName.toString(), createdBy.toString())
            }
            createDialog.dismiss()
        }
    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExits = false
        for (i in musicPlaylist.ref) {
            if (name == i.name) {
                playlistExits = true
                break
            }
        }
        if (playlistExits) {
            Toast.makeText(requireContext(), "Playlist already exists!", Toast.LENGTH_SHORT).show()
        } else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val currentDate = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            tempPlaylist.createdOn = sdf.format(currentDate)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}