package com.tasnim.chowdhury.music.ui.fragments.home

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity.RESULT_OK
import android.app.RecoverableSecurityException
import android.content.Context
import android.database.Cursor
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentMainBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.ui.fragments.player.PlayerFragment
import com.tasnim.chowdhury.music.utilities.SortType
import com.tasnim.chowdhury.music.utilities.closeApp
import com.tasnim.chowdhury.music.utilities.setSongPosition
import com.tasnim.chowdhury.music.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val musicViewModel: MainViewModel by viewModels()
    private var shuffledMusicList: MusicList = MusicList()
    private var storageList: MusicList = MusicList()
    private var sortValue = 0
    private var isMenuOpen = false
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deletedMusicUri: Uri? = null

    companion object {
        var search: Boolean = false
        lateinit var musicListSearch: MusicList
        var mainMusicList: MusicList = MusicList()
        val playPauseIconNP = MutableLiveData<Int>()
        val songDetailsNP = MutableLiveData<Pair<String, String>>()
        var sortOrder: Int = 0
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
        if (activity?.intent?.data?.scheme.contentEquals("content")) {
            storageList = MusicList()
            storageList.add(getMusicDetails(activity?.intent?.data!!))
            val action = MainFragmentDirections.actionMainFragmentToPlayerFragment(0, "Storage", storageList)
            findNavController().navigate(action)
        }

        val sortEditor = activity?.getSharedPreferences("SORT_ORDER", Context.MODE_PRIVATE)
        sortValue = sortEditor?.getInt("sortOrder", 0)!!
        requestAudioPermission()

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    lifecycleScope.launch {
                        deleteMusicFromStorage(deletedMusicUri ?: return@launch)
                    }
                }
                Toast.makeText(
                    context,
                    "Music deleted successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong! Music couldn't be deleted?",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
        getAllSongs(requireContext())
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
        /*
        // Set up layout animation
        val rvAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(requireContext(), R.anim.rv_item_anim)
        )
        rvAnimation.delay = 0.20f
        rvAnimation.order = LayoutAnimationController.ORDER_NORMAL
        // Set layout animation to RecyclerView
        binding.musicListRv.layoutAnimation = rvAnimation
        // Start layout animation
        binding.musicListRv.startLayoutAnimation()*/

        if (sortOrder != sortValue) {
            sortOrder = sortValue
            //musicViewModel.getAllSongs(sortingList, sortOrder)
            when(sortValue) {
                0 -> {
                    musicViewModel.sortMusic(SortType.DATE_DESC)
                }
                1 -> {
                    musicViewModel.sortMusic(SortType.DATE_ASC)
                }
                2 -> {
                    musicViewModel.sortMusic(SortType.TITLE)
                }
                3 -> {
                    musicViewModel.sortMusic(SortType.SIZE_DESC)
                }
                4 -> {
                    musicViewModel.sortMusic(SortType.SIZE_ASC)
                }
            }
        }

        musicAdapter.deleteItem = { position, music ->
            val deleteDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete ${music.title}?")
                .setMessage("Deleting item from here will also delete from your local storage. Are you sure?")
                .setPositiveButton("Yes") { dialog, _ ->
                    lifecycleScope.launch {
                        val file = File(music.path)
                        if (file.exists() && file.isFile) {
                            deleteMusicFromStorage(Uri.parse(music.path))
                            deletedMusicUri = Uri.parse(music.path)
                            musicViewModel.deleteMusic(position, music)
                        }
                    }
                    musicViewModel.deleteMusic(position, music)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
            deleteDialog.create()
            deleteDialog.show()
        }
    }

    fun deleteFile(path: String) {
        val file = File(path)

        if (!file.exists()) return

        if (file.isFile) {
            file.delete()
            return
        }

        val fileArr: Array<File>? = file.listFiles()

        if (fileArr != null) {
            for (subFile in fileArr) {
                if (subFile.isDirectory) {
                    deleteFile(subFile.absolutePath)
                }

                if (subFile.isFile) {
                    subFile.delete()
                }
            }
        }

        file.delete()
    }

    private suspend fun deleteMusicFromStorage(musicUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                requireContext().contentResolver.delete(musicUri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(requireActivity().contentResolver, listOf(musicUri)).intentSender
                    }
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> {
                        null
                    }
                }
                intentSender?.let { iSender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(iSender).build()
                    )
                }
            }
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
            //val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].path
            val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].artUri
            val artist = PlayerFragment.musicList!![PlayerFragment.songPosition].artist

            PlayerFragment.songDetailsLiveData.postValue(Pair(songTitle, artUri))
            PlayerFragment.musicArtist.postValue(artist)
            songDetailsNP.postValue(Pair(songTitle, artUri))
            PlayerFragment.musicService?.showNotification(R.drawable.pause_icon, R.drawable.pause_icon, 1F)
            PlayerFragment.animateDisk.postValue("")

            playMusic()
        }

        binding.nowPlayingView.root.setOnClickListener {
            val nowPlayingAction = MainFragmentDirections.actionMainFragmentToPlayerFragment(
                PlayerFragment.songPosition, "NowPlaying", mainMusicList
            )
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
                    val nowPlayingAction = MainFragmentDirections.actionMainFragmentToPlayerFragment(
                        PlayerFragment.songPosition, "NowPlaying", mainMusicList
                    )
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
                    musicAdapter.submitList(mainMusicList)
                }else {
                    val userInput = newText.lowercase()
                    for (song in mainMusicList) {
                        if (song.title.lowercase().contains(userInput)) {
                            musicListSearch.add(song)
                        }
                    }
                    search = true
                    musicAdapter.submitList(musicListSearch)
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

        binding.feedbackMenuLl.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_feedbackFragment)
            isMenuOpen = false
        }

        binding.aboutMenuLl.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_aboutFragment)
            isMenuOpen = false
        }

        binding.settingsMenuLl.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
            isMenuOpen = false
        }

        binding.exitMenuLl.setOnClickListener {
            closeApp()
        }

        binding.playNextBtn.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_playNextFragment)
        }

        // Menu
        binding.menuBtn.setOnClickListener {
            if (!isMenuOpen) {
                val initialX = 0f
                val finalX = 0.75f * binding.mainCl.width
                val initialMargin = 0
                val finalMargin = 150
                val duration = 500L

                val animator = ValueAnimator.ofFloat(initialX, finalX)
                animator.duration = duration
                animator.addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Float
                    val layoutParams = binding.mainCl.layoutParams as ViewGroup.MarginLayoutParams
                    val hoverParams = binding.mainMenuLayoutHove.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(0, initialMargin + ((finalMargin - initialMargin) * animation.animatedFraction).toInt(), 0, initialMargin + ((finalMargin - initialMargin) * animation.animatedFraction).toInt())
                    hoverParams.setMargins(0, initialMargin + ((finalMargin - initialMargin) * animation.animatedFraction).toInt(), 0, initialMargin + ((finalMargin - initialMargin) * animation.animatedFraction).toInt())
                    binding.mainCl.layoutParams = layoutParams
                    binding.mainMenuLayoutHove.layoutParams = hoverParams
                    binding.mainCl.translationX = animatedValue
                    binding.mainMenuLayoutHove.translationX = animatedValue
                    binding.menuBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.menu_close_icon))

                    binding.mainMenuLayoutHove.visibility = View.VISIBLE
                    binding.mainItemCl.visibility = View.VISIBLE
                    if (PlayerFragment.musicService != null) {
                        binding.nowPlayingView.root.visibility = View.GONE
                    }
                    lifecycleScope.launch {
                        delay(350)
                        binding.sidebarCl.visibility = View.VISIBLE
                    }
                }

                animator.start()
            } else {
                val initialX = 0.75f * binding.mainCl.width
                val finalX = 0f
                val initialMargin = 150
                val finalMargin = 0
                val duration = 500L

                val animator = ValueAnimator.ofFloat(initialX, finalX)
                animator.duration = duration
                animator.addUpdateListener { animation ->
                    val animatedValue = animation.animatedValue as Float
                    val layoutParams = binding.mainCl.layoutParams as ViewGroup.MarginLayoutParams
                    val hoverParams = binding.mainMenuLayoutHove.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt(), 0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt())
                    hoverParams.setMargins(0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt(), 0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt())
                    binding.mainCl.layoutParams = layoutParams
                    binding.mainMenuLayoutHove.layoutParams = hoverParams
                    binding.mainCl.translationX = animatedValue
                    binding.mainMenuLayoutHove.translationX = animatedValue
                    binding.menuBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.menu_open_icon))

                    binding.mainMenuLayoutHove.visibility = View.GONE
                    binding.mainItemCl.visibility = View.VISIBLE
                    if (PlayerFragment.musicService != null) {
                        binding.nowPlayingView.root.visibility = View.VISIBLE
                    }
                    lifecycleScope.launch {
                        delay(200)
                        binding.sidebarCl.visibility = View.GONE
                    }
                }

                animator.start()
            }

            // Toggle the menu state
            isMenuOpen = !isMenuOpen
        }

        binding.mainMenuLayoutHove.setOnClickListener {
            isMenuOpen = false
            val initialX = 0.75f * binding.mainCl.width
            val finalX = 0f
            val initialMargin = 150
            val finalMargin = 0
            val duration = 500L

            val animator = ValueAnimator.ofFloat(initialX, finalX)
            animator.duration = duration
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val layoutParams = binding.mainCl.layoutParams as ViewGroup.MarginLayoutParams
                val hoverParams = binding.mainMenuLayoutHove.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt(), 0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt())
                hoverParams.setMargins(0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt(), 0, initialMargin - ((initialMargin - finalMargin) * animation.animatedFraction).toInt())
                binding.mainCl.layoutParams = layoutParams
                binding.mainMenuLayoutHove.layoutParams = hoverParams
                binding.mainCl.translationX = animatedValue
                binding.mainMenuLayoutHove.translationX = animatedValue
                binding.menuBtn.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.menu_open_icon))

                binding.mainMenuLayoutHove.visibility = View.GONE
                binding.mainItemCl.visibility = View.VISIBLE
                if (PlayerFragment.musicService != null) {
                    binding.nowPlayingView.root.visibility = View.VISIBLE
                }
                lifecycleScope.launch {
                    delay(200)
                    binding.sidebarCl.visibility = View.GONE
                }
            }

            animator.start()
        }

    }

    private fun setObserver() {
        musicViewModel.apply {
            musics.observe(viewLifecycleOwner) {
                musicAdapter.submitList(it)
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
                /*val imageArt = getImageArt(artUri)
                val image = if (imageArt != null) {
                    BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
                } else {
                    BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
                }*/
                Glide.with(requireContext())
                    .load(artUri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher).centerCrop())
                    .into(binding.nowPlayingView.nowPlayingCoverImage)

                binding.nowPlayingView.nowPlayingTitle.text = songTitle
            }
        }
    }

    private fun setNowPlaying() {
        /*val imageArt = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.path?.let {
            getImageArt(
                it
            )
        }
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        }*/
        Glide.with(requireContext())
            .load(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher).centerCrop())
            .into(binding.nowPlayingView.nowPlayingCoverImage)

        binding.nowPlayingView.nowPlayingTitle.text = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.title

        if (PlayerFragment.isPlaying) {
            binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause_icon)
        } else {
            binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play_icon)
        }
    }

    private fun playMusic() {
        PlayerFragment.musicService?.mediaPlayer?.start()
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.pause_icon)
        PlayerFragment.musicService?.showNotification(R.drawable.pause_icon, R.drawable.pause_icon, 1F)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.pause_icon)
        PlayerFragment.isPlaying = true
        PlayerFragment.animateDisk.postValue("Start")
    }

    private fun pauseMusic() {
        PlayerFragment.musicService?.mediaPlayer?.pause()
        binding.nowPlayingView.nowPlayingPlayPauseBtn.setImageResource(R.drawable.play_icon)
        PlayerFragment.musicService?.showNotification(R.drawable.play_icon, R.drawable.play_icon, 0F)
        PlayerFragment.playPauseIconLiveData.postValue(R.drawable.play_icon)
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
                duration = duration, artUri = "UnKnown", path = path.toString(), albumId = "UnKnown", dateAdded = "UnKnown", size = "UnKnown")
        } finally {
            cursor?.close()
        }
    }

    fun getAllSongs(context: Context): ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE
        )
        val cursor = context.applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null,
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val dateAddedC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))
                    val sizeC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        duration = durationC,
                        path = pathC,
                        artUri = artUriC,
                        albumId = albumIdC,
                        dateAdded = dateAddedC,
                        size = sizeC
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                        musicViewModel.insertMusic(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
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