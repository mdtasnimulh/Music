package com.tasnim.chowdhury.music.ui

import android.app.Service
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ServiceCompat.STOP_FOREGROUND_REMOVE
import androidx.core.app.ServiceCompat.StopForegroundFlags
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.ActivityMainBinding
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener{ _, _, _ ->

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!PlayerFragment.isPlaying && PlayerFragment.musicService != null) {
            PlayerFragment.musicService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
            PlayerFragment.musicService?.mediaPlayer?.release()
            PlayerFragment.musicService = null
            exitProcess(1)
        }
    }

}