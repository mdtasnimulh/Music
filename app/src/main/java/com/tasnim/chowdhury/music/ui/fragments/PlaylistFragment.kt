package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.PlaylistAdapter
import com.tasnim.chowdhury.music.databinding.FragmentPlaylistBinding

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var playlistAdapter: PlaylistAdapter

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

    private fun setupAdapter() {
        playlistAdapter = PlaylistAdapter()
        binding.playlistRV.adapter = playlistAdapter
        binding.playlistRV.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupClicks() {
        binding.createPlaylistBtn.setOnClickListener {
            createPlaylistDialog()
        }

    }

    private fun createPlaylistDialog() {
        val view = layoutInflater.inflate(R.layout.playlist_add_dialog, null)
        val createDialog = AlertDialog.Builder(requireContext()).create()
        createDialog.setView(view)

        val playlistNameEt = view.findViewById<TextInputEditText>(R.id.playlistNameEt)
        val userNameEt = view.findViewById<TextInputEditText>(R.id.userNameEt)

        val playlistName = playlistNameEt?.text.toString()
        val userName = userNameEt?.text.toString()

        val addBtn = view.findViewById<AppCompatTextView>(R.id.addBtn)

        addBtn?.setOnClickListener {

            createDialog.dismiss()
        }

        createDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}