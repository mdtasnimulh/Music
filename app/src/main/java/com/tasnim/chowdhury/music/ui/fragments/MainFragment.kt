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
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentMainBinding
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.utilities.setSongPosition
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
        val playPauseIconNP = MutableLiveData<Int>()
        val songDetailsNP = MutableLiveData<Pair<String, String>>()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playPauseIconNP.postValue(0)
        songDetailsNP.postValue(Pair("", ""))
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

    override fun onResume() {
        super.onResume()

        if (PlayerFragment.musicService != null) {
            binding.nowPlayingView.root.visibility = View.VISIBLE
            setNowPlaying()
            binding.nowPlayingView.nowPlayingTitle.isSelected = true
        } else {
            binding.nowPlayingView.root.visibility = View.GONE
        }
    }

    private fun setNowPlaying() {
        Glide.with(requireContext())
            .load(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
            .into(binding.nowPlayingView.nowPlayingCoverImage)

        binding.nowPlayingView.nowPlayingTitle.text = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.title

        if (PlayerFragment.isPlaying) {
            binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.ic_player_pause)
        } else {
            binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.ic_player_play)
        }
    }

    private fun playMusic() {
        PlayerFragment.musicService?.mediaPlayer?.start()
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.ic_player_pause)
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_pause)
        PlayerFragment.isPlaying = true
    }

    private fun pauseMusic() {
        PlayerFragment.musicService?.mediaPlayer?.pause()
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.ic_player_play)
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_play, R.drawable.ic_play)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_play)
        PlayerFragment.isPlaying = false
    }

    private fun onPermissionGranted() {
        initData()
        setupAdapter()
        setupClicks()
        setObserver()
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

        playPauseIconNP.observe(viewLifecycleOwner) { icon ->
            if (icon!=0){
                binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(icon)
            }
        }

        songDetailsNP.observe(viewLifecycleOwner) { (songTitle, artUri) ->
            if (songTitle!="" && artUri!=""){
                Glide.with(requireContext())
                    .load(artUri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                    .into(binding.nowPlayingView.nowPlayingCoverImage)

                binding.nowPlayingView.nowPlayingTitle.text = songTitle
            }
        }
    }

    private fun initData() {
        search = false
    }

    private fun setupAdapter() {
        musicAdapter = MusicAdapter(requireContext(), false)
        binding.musicListRv.adapter = musicAdapter
        binding.musicListRv.setHasFixedSize(true)
        binding.musicListRv.setItemViewCacheSize(15)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.musicListRv.layoutManager = layoutManager

        musicViewModel.getAllSongs()
    }

    private fun setupClicks() {
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setOnClickListener {
            if (PlayerFragment.isPlaying) pauseMusic() else playMusic()
        }

        binding.nowPlayingView.nowPlayingNextBtn.setOnClickListener {
            setSongPosition(increment = true)
            PlayerFragment.musicService?.createMediaPlayer()
            val songTitle = PlayerFragment.musicList!![PlayerFragment.songPosition].title
            val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].artUri

            PlayerFragment.songDetailsLiveData.postValue(Pair(songTitle, artUri))
            songDetailsNP.postValue(Pair(songTitle, artUri))
            PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause)

            playMusic()
        }

        binding.nowPlayingView.root.setOnClickListener {
            val nowPlayingAction = MainFragmentDirections.actionMainFragmentToPlayerFragment(PlayerFragment.songPosition, "NowPlaying", mainMusicList)
            findNavController().navigate(nowPlayingAction)
        }

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
                "NowPlaying" -> {
                    val nowPlayingAction = MainFragmentDirections.actionMainFragmentToPlayerFragment(PlayerFragment.songPosition, "NowPlaying", mainMusicList)
                    findNavController().navigate(nowPlayingAction)
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

        binding.favouriteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_favouritesFragment)
        }

        binding.playListBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_playlistFragment)
        }
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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchView.setQuery("", false)

        playPauseIconNP.postValue(0)
        songDetailsNP.postValue(Pair("", ""))
        _binding = null
    }

}