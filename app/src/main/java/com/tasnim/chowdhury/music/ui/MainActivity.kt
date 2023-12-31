package com.tasnim.chowdhury.music.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.databinding.ActivityMainBinding
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.favourite.FavouritesFragment
import com.tasnim.chowdhury.music.ui.fragments.playlist.PlaylistFragment
import com.tasnim.chowdhury.music.utilities.MusicPlaylist
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object{
        var themeIndex: Int = 0
        val currentTheme = arrayOf(
            R.style.defaultTheme,
            R.style.colorGreen,
            R.style.colorOrange,
            R.style.colorBlue,
            R.style.colorBlack,
            R.style.colorPurple,
            R.style.colorRed,
            R.style.imageTheme,
            R.style.oliveGreenTheme,
            R.style.limeGreenTheme,
            R.style.burntSiennaTheme,
            R.style.redOrangeTheme,
            R.style.blueGrottoTheme,
            R.style.roseRedTheme,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeEditor = getSharedPreferences("THEME", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentTheme[themeIndex])

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        navController.addOnDestinationChangedListener{ _, _, _ ->

        }

        FavouritesFragment.favouriteSongs = ArrayList()
        val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE)
        val jsonString = editor.getString("FavouriteSongs", null)
        val typeToken = object : TypeToken<ArrayList<Music>>(){}.type
        if (jsonString != null) {
            val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavouritesFragment.favouriteSongs.addAll(data)
        }

        PlaylistFragment.musicPlaylist = MusicPlaylist()
        val editorPlaylist = getSharedPreferences("PLAYLIST", Context.MODE_PRIVATE)
        val jsonStringPlaylist = editorPlaylist?.getString("MusicPlaylist", null)
        val typeTokenPlaylist = object : TypeToken<MusicPlaylist>(){}.type
        if (jsonStringPlaylist != null) {
            val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, typeTokenPlaylist)
            PlaylistFragment.musicPlaylist = dataPlaylist
        }

        Log.d("chkPlaylist", "${PlaylistFragment.musicPlaylist}")
    }

}