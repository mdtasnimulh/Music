package com.tasnim.chowdhury.music.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentMainBinding
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val musicViewModel: MainViewModel by viewModels()
    private val mainMusicList = MusicList()
    private var shuffledMusicList: MusicList = MusicList()

    companion object {
        var search: Boolean = false
        lateinit var musicListSearch: MusicList
    }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                onPermissionGranted()
                requestNotificationPermission()
            } else {
                Toast.makeText(requireContext(), "Audio Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions: Map<String, Boolean> ->
            if (permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true &&
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                onPermissionGranted()
                requestNotificationPermission()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {

            } else {

                Toast.makeText(requireContext(), "Notification Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestAudioPermission()
    }

    private fun onPermissionGranted() {
        initData()
        setupAdapter()
        setObserver()
        setupClicks()
    }

    private fun setObserver() {
        musicViewModel.apply {
            musicList.observe(viewLifecycleOwner) {
                musicAdapter.addAll(it)
                mainMusicList.addAll(it)

                val totalSongText = "Total songs: ${it.size}"
                binding.totalSongValue.text = totalSongText
            }
        }
    }

    private fun initData() {
        search = false
    }

    private fun setupAdapter() {
        musicAdapter = MusicAdapter(requireContext())
        binding.musicListRv.adapter = musicAdapter
        binding.musicListRv.setHasFixedSize(true)
        binding.musicListRv.setItemViewCacheSize(15)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.musicListRv.layoutManager = layoutManager

        musicViewModel.getAllSongs()
    }

    private fun setupClicks() {
        musicAdapter.musicItem = { position, tag, _ ->
            when(tag) {
                "MainAdapter" -> {
                    Log.d("chkSongList", "MainAdapter:::$mainMusicList:::")
                    val action = MainFragmentDirections.actionMainFragmentToPlayerFragment(position, "MainAdapter", mainMusicList)
                    findNavController().navigate(action)
                }
                "SearchView" -> {
                    Log.d("chkSongList", "Search:::$musicListSearch:::")
                    val action = MainFragmentDirections.actionMainFragmentToPlayerFragment(position, "SearchView", musicListSearch)
                    findNavController().navigate(action)
                }
            }
        }

        binding.shuffleBtn.setOnClickListener {
            shuffledMusicList.clear()
            shuffledMusicList.addAll(mainMusicList)
            shuffledMusicList.shuffle()
            val shuffleAction = MainFragmentDirections.actionMainFragmentToPlayerFragment(0, "ShuffleButton", shuffledMusicList)
            findNavController().navigate(shuffleAction)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = MusicList()
                Log.d("chkSearchList", "BeforeSearch::${musicListSearch.size}::")
                if (newText.isNullOrBlank()) {
                    search = false
                    musicAdapter.addAll(mainMusicList)
                }else {
                    val userInput = newText.lowercase()
                    for (song in mainMusicList) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.addAll(musicListSearch)
                }
                return true
            }
        })
    }

    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestAudioPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            requestStoragePermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /*private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) !=
                PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_MEDIA_AUDIO)) {
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                        AUDIO_PERMISSION_REQUEST_CODE
                    )
                } else {
                    ActivityCompat.requestPermissions(requireActivity(),
                        arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                        AUDIO_PERMISSION_REQUEST_CODE
                    )
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)

                } else {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
                }
            }
        }
    }*/

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchView.setQuery("", false)
        _binding = null
    }

}