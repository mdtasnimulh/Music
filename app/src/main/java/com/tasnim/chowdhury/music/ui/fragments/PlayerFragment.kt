package com.tasnim.chowdhury.music.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentPlayerBinding
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.services.MusicService
import com.tasnim.chowdhury.music.utilities.animateElevation
import com.tasnim.chowdhury.music.utilities.animateMargins
import com.tasnim.chowdhury.music.utilities.animateRotation
import com.tasnim.chowdhury.music.utilities.closeApp
import com.tasnim.chowdhury.music.utilities.favouriteSongChecker
import com.tasnim.chowdhury.music.utilities.formatDuration
import com.tasnim.chowdhury.music.utilities.getImageArt
import com.tasnim.chowdhury.music.utilities.setSongPosition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PlayerFragment : Fragment(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private lateinit var binding: FragmentPlayerBinding
    //private val binding get() = _binding!!
    private val args by navArgs<PlayerFragmentArgs>()

    private var min15: Boolean = false
    private var min20: Boolean = false
    private var min25: Boolean = false
    private var min30: Boolean = false
    private var min60: Boolean = false
    private var min1: Boolean = false

    private val rotationHandler = Handler()
    private var isRotationRunning = false
    private var currentRotation: Float = 0f
    private val rotationUpdateInterval = 16L

    companion object {
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var musicList: MusicList? = null
        val playPauseLiveData = MutableLiveData<Pair<String, Int>>()
        val playPauseIconLiveData = MutableLiveData<Int>()
        val songDetailsLiveData = MutableLiveData<Pair<String, String>>()
        val startTimeLiveData = MutableLiveData<Long>()
        val endTimeLiveData = MutableLiveData<Long>()
        val initialProgressLiveData = MutableLiveData<Int>()
        val progressMaxLiveData = MutableLiveData<Int>()
        var repeat: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
        val favouriteIcon = MutableLiveData<Int>()
        val animateDisk = MutableLiveData<String>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postInitialValues()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //startService()
        initializeData()
        setupClicks()
        setUpObservers()

    }

    override fun onResume() {
        super.onResume()

        val editor = activity?.getSharedPreferences("FAVOURITES", AppCompatActivity.MODE_PRIVATE)?.edit()
        val jsonString = GsonBuilder().create().toJson(FavouritesFragment.favouriteSongs)
        editor?.putString("FavouriteSongs", jsonString)
        editor?.apply()
    }

    private fun postInitialValues() {
        playPauseLiveData.postValue(Pair("", 0))
        playPauseIconLiveData.postValue(0)
        songDetailsLiveData.postValue(Pair("", ""))
        startTimeLiveData.postValue(0L)
        endTimeLiveData.postValue(0L)
        initialProgressLiveData.postValue(0)
        progressMaxLiveData.postValue(0)
        //animateDisk.postValue("")
    }

    private fun setUpObservers() {
        playPauseLiveData.observe(viewLifecycleOwner) { (tag, icon) ->
            Log.d("chkTitleCover", "tag:$tag, icon:$icon")
            if (icon != 0){
                binding.playPauseBtn.setImageResource(icon)
            }
        }
        songDetailsLiveData.observe(viewLifecycleOwner) { (songTitle, artUri) ->
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
                    .into(binding.songCoverImage)
                binding.playerSongTitle.text = songTitle
            }
        }

        playPauseIconLiveData.observe(viewLifecycleOwner) { icon ->
            if (icon!=0){
                binding.playPauseBtn.setImageResource(icon)
            }
        }

        startTimeLiveData.observe(viewLifecycleOwner) { startTime ->
            if (startTime!=0L) {
                binding.startTimeSeekBar.text = formatDuration(startTime.toLong())
            }
        }

        endTimeLiveData.observe(viewLifecycleOwner) { endTime ->
            if (endTime!=0L) {
                binding.endTimeSeekbar.text = formatDuration(endTime.toLong())
            }
        }

        initialProgressLiveData.observe(viewLifecycleOwner) { progress ->
            if (progress!=0) {
                binding.seekBar.progress = progress
            }
        }

        progressMaxLiveData.observe(viewLifecycleOwner) { max ->
            if (max!=0) {
                binding.seekBar.max = max
            }
        }

        favouriteIcon.observe(viewLifecycleOwner) {
            if (it != null){
                binding.favBtn.setImageResource(it)
            }
        }

        animateDisk.observe(viewLifecycleOwner) {
            when(it) {
                "Start" -> {
                    binding.musicPlayerHandler.apply {
                        animateMargins(
                            startMarginEnd = 16.dpToPx(),
                            endMarginEnd = 58.dpToPx(),
                            startMarginTop = (-11).dpToPx(),
                            endMarginTop = (-34).dpToPx(),
                            duration = 500
                        )
                        animateElevation(25.dpToPx().toFloat(), 500)
                        animateRotation(35f, 500)
                    }
                    startRotationAnimation()
                }
                "Stop" -> {
                    binding.musicPlayerHandler.apply {
                        animateMargins(
                            startMarginEnd = 58.dpToPx(),
                            endMarginEnd = 16.dpToPx(),
                            startMarginTop = (-34).dpToPx(),
                            endMarginTop = (-11).dpToPx(),
                            duration = 500
                        )
                        animateElevation(0.dpToPx().toFloat(), 500)
                        animateRotation(0f, 500)
                    }
                    stopRotationAnimation()
                }
                "" -> {
                    binding.musicPlayerHandler.apply {
                        animateMargins(
                            startMarginEnd = 58.dpToPx(),
                            endMarginEnd = 58.dpToPx(),
                            startMarginTop = (-34).dpToPx(),
                            endMarginTop = (-34).dpToPx(),
                            duration = 0
                        )
                        animateElevation(25.dpToPx().toFloat(), 500)
                        animateRotation(35f, 500)
                    }
                    startRotationAnimation()
                }
            }
        }
    }

    private fun startService() {
        val intent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(intent, this, BIND_AUTO_CREATE)
        requireContext().startService(intent)
    }

    private fun initializeData() {
        when(args.tag) {
            "Storage" -> {
                musicList = args.musicList
                songPosition = args.position
                Log.d("chkStorageMusic", "${musicList}:::\n${args.position}")
                startService()
            }
            "MainAdapter" -> {
                musicList = args.musicList
                songPosition = args.position
                startService()
            }
            "FavouriteAdapter" -> {
                musicList = args.musicList
                songPosition = args.position
                startService()
            }
            "NowPlaying" -> {
                binding.startTimeSeekBar.text = musicService?.mediaPlayer?.currentPosition?.let {
                    formatDuration(it.toLong())
                }
                binding.endTimeSeekbar.text = musicService?.mediaPlayer?.duration?.let {
                    formatDuration(it.toLong())
                }
                binding.seekBar.progress = musicService?.mediaPlayer?.currentPosition!!
                binding.seekBar.max = musicService?.mediaPlayer?.duration!!
                if (isPlaying) {
                    binding.playPauseBtn.setImageResource(R.drawable.ic_player_pause)
                } else {
                    binding.playPauseBtn.setImageResource(R.drawable.ic_player_play)
                }
            }
            "SearchView" -> {
                musicList = args.musicList
                songPosition = args.position
                startService()
            }
            "ShuffleButton" -> {
                musicList = args.musicList
                songPosition = 0
                args.musicList
                startService()
            }
            "ShuffleFavourites" -> {
                musicList = args.musicList
                songPosition = 0
                args.musicList
                startService()
            }
            "ShufflePlaylistSongs" -> {
                musicList = args.musicList
                songPosition = 0
                args.musicList
                startService()
            }
            "PlaylistDetails" -> {
                musicList = args.musicList
                songPosition = args.position
                startService()
            }
        }
        setLayout()
    }

    private fun setupClicks() {
        binding.playPauseBtn.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.prevSongBtn.setOnClickListener {
            prevNextSong(increment = false)
        }

        binding.nextSongBtn.setOnClickListener {
            prevNextSong(increment = true)
        }

        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService?.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { /*DO NOTHING*/ }

            override fun onStopTrackingTouch(p0: SeekBar?) { /*DO NOTHING*/ }
        })

        binding.repeatBtn.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            } else {
                repeat = false
                binding.repeatBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
            }
        }

        binding.equalizerBtn.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService?.mediaPlayer?.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 641)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Equalizer Feature is not supported for your mobile, Sorry!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.exitTimerBtn.setOnClickListener {
            val sleepTimer = min1 || min15 || min20 || min25 || min30 || min60
            if (!sleepTimer) {
                showTimerBottomSheet()
            } else {
                val cancelDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Cancel Timer")
                    .setMessage("Are you sure you want to cancel the sleep timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min1 = false
                        min15 = false
                        min20 = false
                        min25 = false
                        min30 = false
                        min60 = false
                        binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                cancelDialog.create()
                cancelDialog.show()
            }
        }

        binding.shareBtn.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicList?.get(songPosition)?.path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }

        binding.backImg.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.favBtn.setOnClickListener {
            if (isFavourite){
                isFavourite = false
                binding.favBtn.setImageResource(R.drawable.ic_favourite_outline)
                FavouritesFragment.favouriteSongs.removeAt(fIndex)
            } else {
                isFavourite = true
                binding.favBtn.setImageResource(R.drawable.ic_favourite_filled)
                FavouritesFragment.favouriteSongs.add(musicList!![songPosition])
            }
        }
    }

    private fun setLayout(){
        fIndex = favouriteSongChecker(musicList!![songPosition].id)
        val imageArt = getImageArt(args.musicList[songPosition].path)
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        }
        Glide.with(requireContext())
            .load(image)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
            .into(binding.songCoverImage)
        binding.playerSongTitle.text = args.musicList[songPosition].title
        if (repeat) {
            binding.repeatBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        }
        if (min1 || min15 || min20 || min25 || min30 || min60) {
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        } else {
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
        }
        if (isFavourite){
            binding.favBtn.setImageResource(R.drawable.ic_favourite_filled)
        } else {
            binding.favBtn.setImageResource(R.drawable.ic_favourite_outline)
        }

        binding.musicPlayerHandler.apply {
            animateMargins(
                startMarginEnd = 58.dpToPx(),
                endMarginEnd = 58.dpToPx(),
                startMarginTop = (-34).dpToPx(),
                endMarginTop = (-34).dpToPx(),
                duration = 0
            )
            animateElevation(25.dpToPx().toFloat(), 500)
            animateRotation(35f, 500)
        }
        startRotationAnimation()
    }

    private fun createMediaPlayer() {
        try {
            if (musicService?.mediaPlayer == null) {
                musicService?.mediaPlayer = MediaPlayer()
            }
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource(args.musicList[songPosition].path)
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()
            isPlaying = true
            binding.playPauseBtn.setImageResource(R.drawable.ic_player_pause)
            musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
            binding.startTimeSeekBar.text = musicService?.mediaPlayer?.currentPosition?.toLong()
                ?.let { formatDuration(it) }
            binding.endTimeSeekbar.text = musicService?.mediaPlayer?.duration?.toLong()
                ?.let { formatDuration(it) }
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService?.mediaPlayer?.duration!!
            musicService?.mediaPlayer?.setOnCompletionListener(this)
            nowPlayingId = musicList?.get(songPosition)?.id.toString()
        }catch (_: Exception) { }
    }

    private fun playMusic() {
        binding.playPauseBtn.setImageResource(R.drawable.ic_player_pause)
        musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
        isPlaying = true
        musicService?.mediaPlayer?.start()

        binding.musicPlayerHandler.apply {
            animateMargins(
                startMarginEnd = 16.dpToPx(),
                endMarginEnd = 58.dpToPx(),
                startMarginTop = (-11).dpToPx(),
                endMarginTop = (-34).dpToPx(),
                duration = 500
            )
            animateElevation(25.dpToPx().toFloat(), 500)
            animateRotation(35f, 500)
            animateDisk.postValue("Start")
        }
        startRotationAnimation()
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setImageResource(R.drawable.ic_player_play)
        musicService?.showNotification(R.drawable.ic_player_play, R.drawable.ic_play, 0F)
        isPlaying = false
        musicService?.mediaPlayer?.pause()

        binding.musicPlayerHandler.apply {
            animateMargins(
                startMarginEnd = 58.dpToPx(),
                endMarginEnd = 16.dpToPx(),
                startMarginTop = (-34).dpToPx(),
                endMarginTop = (-11).dpToPx(),
                duration = 500
            )
            animateElevation(0.dpToPx().toFloat(), 500)
            animateRotation(0f, 500)
            animateDisk.postValue("Stop")
        }
        stopRotationAnimation()
    }

    private fun Int.dpToPx(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
    }

    private val rotationRunnable = object : Runnable {
        override fun run() {
            currentRotation += 1f
            if (currentRotation >= 360f) {
                currentRotation = 0f
            }
            binding.songCoverImage.rotation = currentRotation
            if (isRotationRunning) {
                rotationHandler.postDelayed(this, rotationUpdateInterval)
            }
        }
    }

    private fun startRotationAnimation() {
        if (!isRotationRunning) {
            isRotationRunning = true
            rotationHandler.post(rotationRunnable)
        }
    }

    private fun stopRotationAnimation() {
        isRotationRunning = false
        rotationHandler.removeCallbacks(rotationRunnable)
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService?.seekBarSetup()
        musicService?.audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService?.audioManager?.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        MainFragment.songDetailsNP.postValue(musicList?.get(songPosition)?.let { Pair(it.title, it.path) })
        if (isPlaying) {
            musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
        }
        try {
            setLayout()
        }catch (_: Exception) {}
    }

    private fun showTimerBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout)
        bottomSheetDialog.show()
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timer15MinLl)?.setOnClickListener {
            min15 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(15*60000)
                if (min15) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 15 minutes!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timer20MinLl)?.setOnClickListener {
            min20 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(20*60000)
                if (min20) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 20 minutes!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timer25MinLl)?.setOnClickListener {
            min25 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(25*60000)
                if (min25) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 25 minutes!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timer30MinLl)?.setOnClickListener {
            min30 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(30*60000)
                if (min30) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 30 minutes!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timer60MinLl)?.setOnClickListener {
            min60 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(60*60000)
                if (min60) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 60 minutes!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayoutCompat>(R.id.timerTestMinLl)?.setOnClickListener {
            min1 = true
            binding.exitTimerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
            lifecycleScope.launch {
                delay(5*1000)
                if (min1) {
                    closeApp()
                }
            }
            Toast.makeText(
                requireContext(),
                "Music will stop after 1 minute!",
                Toast.LENGTH_SHORT
            ).show()
            bottomSheetDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 641 || resultCode == RESULT_OK) {
            return
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isPlaying) {
            animateDisk.postValue("")
        }
        //_binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        postInitialValues()
        if ((musicList?.get(songPosition)?.id ?: 0) == "UnKnown") {
            closeApp()
        }
    }

}