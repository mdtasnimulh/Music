package com.tasnim.chowdhury.music.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
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

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PlayerFragmentArgs>()
    private var songPosition: Int = 0

    companion object {
        var mediaPlayer: MediaPlayer? = null
        var isPlaying: Boolean = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeData()
        setupClicks()

    }

    private fun initializeData() {
        when(args.tag) {
            "MainAdapter" -> {
                songPosition = args.position
                Log.d("PlayerFragment", "$songPosition main")
            }
            "ShuffleButton" -> {
                songPosition = 0
                args.musicList
                Log.d("PlayerFragment", "$songPosition shuffle")
            }
        }
        setLayout()
        createMediaPlayer()
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
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(args.musicList[songPosition].path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            isPlaying = true
            binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
        Log.d("PlayerFragment", "$songPosition *-*")
    }

    private fun setSongPosition(increment: Boolean) {
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
    }

    private fun playMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
        isPlaying = true
        mediaPlayer?.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setIconResource(R.drawable.ic_play)
        isPlaying = false
        mediaPlayer?.pause()
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
        _binding = null
    }

}