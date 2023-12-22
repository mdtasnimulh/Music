package com.tasnim.chowdhury.music.ui.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.tasnim.chowdhury.music.databinding.MusicListItemBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.services.MusicService
import com.tasnim.chowdhury.music.utilities.getImageArt
import com.tasnim.chowdhury.music.utilities.setSongPosition
import com.tasnim.chowdhury.music.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val musicViewModel: MainViewModel by viewModels()
    //private val mainMusicList = MusicList()
    private var shuffledMusicList: MusicList = MusicList()
    private var storageList: MusicList = MusicList()
    private var sortValue = 0

    companion object {
        var search: Boolean = false
        lateinit var musicListSearch: MusicList
        var mainMusicList: MusicList = MusicList()
        val playPauseIconNP = MutableLiveData<Int>()
        val songDetailsNP = MutableLiveData<Pair<String, String>>()
        var sortOrder: Int = 0
        val sortingList: ArrayList<String> = arrayListOf(
            MediaStore.Audio.Media.DATE_ADDED + " DESC",
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC"
        )
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
        //startService()
        if (activity?.intent?.data?.scheme.contentEquals("content")) {
            storageList = MusicList()
            storageList.add(getMusicDetails(activity?.intent?.data!!))
            val action = MainFragmentDirections.actionMainFragmentToPlayerFragment(0, "Storage", storageList)
            findNavController().navigate(action)
        }

        val sortEditor = activity?.getSharedPreferences("SORT_ORDER", Context.MODE_PRIVATE)
        sortValue = sortEditor?.getInt("sortOrder", 0)!!
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

    private fun onPermissionGranted() {
        initData()
        setupAdapter()
        setObserver()
        setupClicks()
    }

    private fun initData() {
        search = false
        val sortEditor = activity?.getSharedPreferences("SORT_ORDER", Context.MODE_PRIVATE)
        sortValue = sortEditor?.getInt("sortOrder", 0)!!
    }

    private fun setupAdapter() {
        musicAdapter = MusicAdapter(requireContext(), false)
        binding.musicListRv.adapter = musicAdapter
        binding.musicListRv.setHasFixedSize(true)
        binding.musicListRv.setItemViewCacheSize(15)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.musicListRv.layoutManager = layoutManager

        if (sortOrder != sortValue) {
            sortOrder = sortValue
            musicViewModel.getAllSongs(sortingList, sortOrder)
        }else {
            musicViewModel.getAllSongs(sortingList, sortOrder)
        }
    }

    private fun setupClicks() {
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setOnClickListener {
            if (PlayerFragment.isPlaying) pauseMusic() else playMusic()
        }

        binding.nowPlayingView.nowPlayingNextBtn.setOnClickListener {
            setSongPosition(increment = true)
            PlayerFragment.musicService?.createMediaPlayer()
            val songTitle = PlayerFragment.musicList!![PlayerFragment.songPosition].title
            val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].path

            PlayerFragment.songDetailsLiveData.postValue(Pair(songTitle, artUri))
            songDetailsNP.postValue(Pair(songTitle, artUri))
            PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
            PlayerFragment.animateDisk.postValue("")

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

        binding.feedbackBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_feedbackFragment)
        }

        binding.aboutBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_aboutFragment)
        }

        binding.settingsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        binding.playNextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_playNextFragment)
        }

    }

    private fun setObserver() {
        musicViewModel.apply {
            musicList.observe(viewLifecycleOwner) {
                musicAdapter.addAll(it)
                mainMusicList.clear()
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
                val imageArt = getImageArt(artUri)
                val image = if (imageArt != null) {
                    BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
                } else {
                    BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
                }
                Glide.with(requireContext())
                    .load(image)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                    .into(binding.nowPlayingView.nowPlayingCoverImage)

                binding.nowPlayingView.nowPlayingTitle.text = songTitle
            }
        }
    }

    private fun setNowPlaying() {
        val imageArt = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.path?.let {
            getImageArt(
                it
            )
        }
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        }
        Glide.with(requireContext())
            .load(image)
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
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_pause)
        PlayerFragment.isPlaying = true
        PlayerFragment.animateDisk.postValue("Start")
    }

    private fun pauseMusic() {
        PlayerFragment.musicService?.mediaPlayer?.pause()
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.ic_player_play)
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_play, R.drawable.ic_play, 0F)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_play)
        PlayerFragment.isPlaying = false
        PlayerFragment.animateDisk.postValue("Stop")
    }

    private fun getMusicDetails(contentUri: Uri): Music {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = requireContext().contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor?.moveToFirst()
            val path = dataColumn?.let { cursor?.getString(it) }
            val duration = durationColumn?.let { cursor?.getLong(it) }!!
            return Music(id = "UnKnown", title = path.toString(), album = "UnKnown", artist = "UnKnown",
                duration = duration, artUri = "UnKnown", path = path.toString())
        } finally {
            cursor?.close()
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

        playPauseIconNP.postValue(0)
        songDetailsNP.postValue(Pair("", ""))
        _binding = null
    }

}