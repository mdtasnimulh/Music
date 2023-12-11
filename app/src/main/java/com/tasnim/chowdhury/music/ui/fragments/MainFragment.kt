package com.tasnim.chowdhury.music.ui.fragments

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tasnim.chowdhury.music.adapters.MusicAdapter
import com.tasnim.chowdhury.music.databinding.FragmentMainBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicList
import com.tasnim.chowdhury.music.repository.MainRepository
import com.tasnim.chowdhury.music.utilities.Constants.AUDIO_PERMISSION_REQUEST_CODE
import com.tasnim.chowdhury.music.utilities.Constants.STORAGE_PERMISSION_REQUEST_CODE
import com.tasnim.chowdhury.music.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var musicAdapter: MusicAdapter
    private val musicViewModel: MainViewModel by viewModels()
    private val allMusic = MusicList()

    companion object {
        //var musicListMF: MutableList<Music> = mutableListOf()
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

        requestStoragePermission()
        onPermissionGranted()
    }

    private fun onPermissionGranted() {
        setupAdapter()
        initData()
        setObserver()
        setupClicks()
    }

    private fun setObserver() {
        musicViewModel.apply {
            musicList.observe(viewLifecycleOwner) {
                musicAdapter.addAll(it)
                allMusic.addAll(it)

                val totalSongText = "Total songs: ${it.size}"
                binding.totalSongValue.text = totalSongText
            }
        }
    }

    private fun initData() {

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
        musicAdapter.musicItem = { position, tag, song ->
            Log.d("chkSongList", ":::$allMusic:::")
            val action = MainFragmentDirections.actionMainFragmentToPlayerFragment(position, tag, allMusic)
            findNavController().navigate(action)
        }
    }

    private fun requestStoragePermission() {
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Audio Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Audio Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(requireContext(), "Storage Permission Granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}