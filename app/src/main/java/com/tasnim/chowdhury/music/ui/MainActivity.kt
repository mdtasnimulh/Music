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
import com.tasnim.chowdhury.music.ui.fragments.FavouritesFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.ui.fragments.PlaylistFragment
import com.tasnim.chowdhury.music.utilities.MusicPlaylist
import com.tasnim.chowdhury.music.utilities.closeApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object{
        var themeIndex: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeEditor = getSharedPreferences("THEME", MODE_PRIVATE)
        themeEditor.getInt("themeIndex", 0)

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

    override fun onDestroy() {
        super.onDestroy()

        /*val editor = getSharedPreferences("FAVOURITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavouritesFragment.favouriteSongs)
        editor.putString("FavouriteSongs", jsonString)
        editor.apply()*/
        /*val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistFragment.musicPlaylist)
        editor?.putString("MusicPlaylist", jsonStringPlaylist)*/

        if (!PlayerFragment.isPlaying && PlayerFragment.musicService != null){
            closeApp()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Toast.makeText(this@MainActivity, "${newText.toString()}", Toast.LENGTH_SHORT).show()
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }*/

}