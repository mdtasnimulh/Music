package com.tasnim.chowdhury.music.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.FragmentPlayerBinding
import com.tasnim.chowdhury.music.model.Music

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<PlayerFragmentArgs>()
    private lateinit var musicListPf: ArrayList<Music>

    companion object {
        var songPosition: Int = 0
        var mediaPlayer: MediaPlayer? = null
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
    }

    private fun initializeData() {
        when(args.tag) {
            "MainAdapter" -> {
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer()
                }
                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(args.musicList[args.position].path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}