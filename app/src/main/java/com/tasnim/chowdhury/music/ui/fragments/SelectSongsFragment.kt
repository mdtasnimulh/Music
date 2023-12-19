package com.tasnim.chowdhury.music.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentSelectSongsBinding
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.ui.fragments.MainFragment.Companion.mainMusicList

class SelectSongsFragment : Fragment() {

    private var _binding: FragmentSelectSongsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupClicks()
    }

    private fun setupAdapter() {
        adapter = MusicAdapter(requireContext(), selectionFragment = true)
        binding.selectSongsRv.adapter = adapter
        binding.selectSongsRv.setHasFixedSize(true)
        binding.selectSongsRv.setItemViewCacheSize(10)
        binding.selectSongsRv.layoutManager = LinearLayoutManager(requireContext())
        adapter.addAll(mainMusicList)
    }

    private fun setupClicks() {
        binding.selectSongBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.selectSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.selectSearchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                MainFragment.musicListSearch = MusicList()
                Log.d("chkSearchList", "BeforeSearch::${MainFragment.musicListSearch.size}::")
                if (newText.isNullOrBlank()) {
                    MainFragment.search = false
                    adapter.addAll(mainMusicList)
                }else {
                    val userInput = newText.lowercase()
                    for (song in mainMusicList) {
                        if (song.title.lowercase().contains(userInput)) {
                            MainFragment.musicListSearch.add(song)
                        }
                    }
                    MainFragment.search = true
                    adapter.addAll(MainFragment.musicListSearch)
                }
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}