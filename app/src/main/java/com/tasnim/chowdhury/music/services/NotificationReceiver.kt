package com.tasnim.chowdhury.music.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.ui.fragments.MainFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.utilities.Constants.EXIT
import com.tasnim.chowdhury.music.utilities.Constants.NEXT
import com.tasnim.chowdhury.music.utilities.Constants.PLAY
import com.tasnim.chowdhury.music.utilities.Constants.PREVIOUS
import com.tasnim.chowdhury.music.utilities.closeApp
import com.tasnim.chowdhury.music.utilities.favouriteSongChecker
import com.tasnim.chowdhury.music.utilities.setSongPosition
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            PREVIOUS -> {
                prevNextSong(increment = false, context = context!!)
                Toast.makeText(context, "Previous", Toast.LENGTH_SHORT).show()
            }
            PLAY -> {
                if (PlayerFragment.isPlaying){
                    pauseMusic()
                    Toast.makeText(context, "pause", Toast.LENGTH_SHORT).show()
                }else{
                    playMusic()
                    Toast.makeText(context, "play", Toast.LENGTH_SHORT).show()
                }
            }
            NEXT -> {
                prevNextSong(increment = true, context!!)
                Toast.makeText(context, "next", Toast.LENGTH_SHORT).show()
            }
            EXIT -> {
                closeApp()
            }
        }
    }

    private fun playMusic() {
        PlayerFragment.isPlaying = true
        PlayerFragment.musicService?.mediaPlayer?.start()
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause)
        PlayerFragment.playPauseLiveData.postValue(Pair("play", R.drawable.ic_player_pause))
        MainFragment.playPauseIconNP.postValue(R.drawable.ic_player_pause)
    }

    private fun pauseMusic() {
        PlayerFragment.isPlaying = false
        PlayerFragment.musicService?.mediaPlayer?.pause()
        PlayerFragment.musicService?.showNotification(R.drawable.ic_player_play, R.drawable.ic_play)
        PlayerFragment.playPauseLiveData.postValue(Pair("pause", R.drawable.ic_player_play))
        MainFragment.playPauseIconNP.postValue(R.drawable.ic_player_play)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerFragment.musicService?.createMediaPlayer()
        val songTitle = PlayerFragment.musicList!![PlayerFragment.songPosition].title
        val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].artUri

        PlayerFragment.songDetailsLiveData.postValue(Pair(songTitle, artUri))
        MainFragment.songDetailsNP.postValue(Pair(songTitle, artUri))

        playMusic()

        PlayerFragment.fIndex = favouriteSongChecker(PlayerFragment.musicList!![PlayerFragment.songPosition].id)
        if (PlayerFragment.isFavourite) {
            PlayerFragment.favouriteIcon.postValue(R.drawable.ic_favourite_filled)
        } else {
            PlayerFragment.favouriteIcon.postValue(R.drawable.ic_favourite_outline)
        }
    }

}