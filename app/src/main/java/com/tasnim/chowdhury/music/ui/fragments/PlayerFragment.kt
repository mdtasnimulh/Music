package com.tasnim.chowdhury.music.ui.fragments

import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentPlayerBinding
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.services.MusicService
import com.tasnim.chowdhury.music.utilities.formatDuration
import com.tasnim.chowdhury.music.utilities.setSongPosition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment(), ServiceConnection, MediaPlayer.OnCompletionListener {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PlayerFragmentArgs>()
    //private var songPosition: Int = 0
    //private var musicService: MusicServices? = null

    companion object {
        /*@SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentPlayerBinding*/
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postInitialValues()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startService()
        initializeData()
        setupClicks()
        setUpObservers()

    }

    private fun postInitialValues() {
        playPauseLiveData.postValue(Pair("", 0))
        playPauseIconLiveData.postValue(0)
        songDetailsLiveData.postValue(Pair("", ""))
        startTimeLiveData.postValue(0L)
        endTimeLiveData.postValue(0L)
        initialProgressLiveData.postValue(0)
        progressMaxLiveData.postValue(0)
    }

    private fun setUpObservers() {
        playPauseLiveData.observe(viewLifecycleOwner) { (tag, icon) ->
            Log.d("chkTitleCover", "tag:$tag, icon:$icon")
            if (icon != 0){
                binding.playPauseBtn.setIconResource(icon)
            }
        }
        songDetailsLiveData.observe(viewLifecycleOwner) { (songTitle, artUri) ->
            Log.d("chkTitleCover", "songTitle:$songTitle, artUri:$artUri")
            if (songTitle!="" && artUri!=""){
                Glide.with(requireContext())
                    .load(artUri)
                    .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
                    .into(binding.songCoverImage)
                binding.playerSongTitle.text = songTitle
                Log.d("chkTitleCover", "$songTitle *-*-*")
            }
        }

        playPauseIconLiveData.observe(viewLifecycleOwner) { icon ->
            Log.d("chkTitleCover", "icon:$icon")
            if (icon!=0){
                binding.playPauseBtn.setIconResource(icon)
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
    }

    private fun startService() {
        val intent = Intent(requireContext(), MusicService::class.java)
        requireContext().bindService(intent, this, BIND_AUTO_CREATE)
        requireContext().startService(intent)
    }

    private fun initializeData() {
        when(args.tag) {
            "MainAdapter" -> {
                musicList = args.musicList
                songPosition = args.position
                Log.d("PlayerFragment", "$songPosition main")
            }
            "ShuffleButton" -> {
                musicList = args.musicList
                songPosition = 0
                args.musicList
                Log.d("PlayerFragment", "$songPosition shuffle")
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
    }

    private fun setLayout(){
        Glide.with(requireContext())
            .load(args.musicList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
            .into(binding.songCoverImage)
        Log.d("chkTitleCover", "${binding.songCoverImage}")
        binding.playerSongTitle.text = args.musicList[songPosition].title
        Log.d("chkTitleCover", "${args.musicList[songPosition].title}")
        if (repeat) {
            binding.repeatBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        }
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
            binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
            musicService?.showNotification(R.drawable.ic_pause)
            binding.startTimeSeekBar.text = musicService?.mediaPlayer?.currentPosition?.toLong()
                ?.let { formatDuration(it) }
            binding.endTimeSeekbar.text = musicService?.mediaPlayer?.duration?.toLong()
                ?.let { formatDuration(it) }
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService?.mediaPlayer?.duration!!
            musicService?.mediaPlayer?.setOnCompletionListener(this)
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
        Log.d("PlayerFragment", "$songPosition *-*")
    }

    private fun playMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        musicService?.showNotification(R.drawable.ic_pause)
        isPlaying = true
        musicService?.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        musicService?.showNotification(R.drawable.ic_play)
        isPlaying = false
        musicService?.mediaPlayer?.pause()
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

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e: Exception) {
            Log.d("catchException", ":::${e.message}:::")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        postInitialValues()
    }

}