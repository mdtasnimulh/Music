package com.tasnim.chowdhury.music.ui.fragments

import android.annotation.SuppressLint
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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentPlayerBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.services.MusicService
import com.tasnim.chowdhury.music.services.MusicServices
import com.tasnim.chowdhury.music.utilities.setSongPosition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayerFragment : Fragment(), ServiceConnection {

    /*private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!*/
    private val args by navArgs<PlayerFragmentArgs>()
    //private var songPosition: Int = 0
    //private var musicService: MusicServices? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentPlayerBinding
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var musicList: MusicList? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startService()
        initializeData()
        setupClicks()

    }

    private fun startService() {
        // for starting the service
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
    }

    private fun setLayout(){
        Glide.with(requireContext())
            .load(args.musicList[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.ic_launcher_background).centerCrop())
            .into(binding.songCoverImage)
        binding.playerSongTitle.text = args.musicList[songPosition].title
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
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
        Log.d("PlayerFragment", "$songPosition *-*")
    }

    /*private fun setSongPosition(increment: Boolean) {
        if (increment) {
            if (args.musicList.size - 1 == songPosition) {
                songPosition = 0
            } else {
                ++songPosition
            }
        } else {
            if (songPosition == 0) {
                songPosition = args.musicList.size - 1
            } else {
                --songPosition
            }
        }
    }*/

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

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

}