package com.tasnim.chowdhury.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.ui.fragments.home.MainFragment
import com.tasnim.chowdhury.music.ui.fragments.player.PlayerFragment
import com.tasnim.chowdhury.music.utilities.Constants.EXIT
import com.tasnim.chowdhury.music.utilities.Constants.NEXT
import com.tasnim.chowdhury.music.utilities.Constants.PLAY
import com.tasnim.chowdhury.music.utilities.Constants.PREVIOUS
import com.tasnim.chowdhury.music.utilities.closeApp
import com.tasnim.chowdhury.music.utilities.favouriteSongChecker
import com.tasnim.chowdhury.music.utilities.setSongPosition

class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            PREVIOUS -> {
                prevNextSong(increment = false, context = context!!)
            }
            PLAY -> {
                if (PlayerFragment.isPlaying){
                    pauseMusic()
                }else{
                    playMusic()
                }
            }
            NEXT -> {
                prevNextSong(increment = true, context!!)
            }
            EXIT -> {
                closeApp()
            }
        }
    }

    private fun playMusic() {
        PlayerFragment.isPlaying = true
        PlayerFragment.musicService?.mediaPlayer?.start()
        PlayerFragment.musicService?.showNotification(R.drawable.pause_icon, R.drawable.pause_icon, 1F)
        PlayerFragment.playPauseLiveData.postValue(Pair("play", R.drawable.pause_icon))
        MainFragment.playPauseIconNP.postValue(R.drawable.pause_icon)
        PlayerFragment.animateDisk.postValue("Start")
    }

    private fun pauseMusic() {
        PlayerFragment.isPlaying = false
        PlayerFragment.musicService?.mediaPlayer?.pause()
        PlayerFragment.musicService?.showNotification(R.drawable.play_icon, R.drawable.play_icon, 0F)
        PlayerFragment.playPauseLiveData.postValue(Pair("pause", R.drawable.play_icon))
        MainFragment.playPauseIconNP.postValue(R.drawable.play_icon)
        PlayerFragment.animateDisk.postValue("Stop")
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerFragment.musicService?.createMediaPlayer()
        val songTitle = PlayerFragment.musicList!![PlayerFragment.songPosition].title
        //val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].path
        val artUri = PlayerFragment.musicList!![PlayerFragment.songPosition].artUri
        val artist = PlayerFragment.musicList!![PlayerFragment.songPosition].artist

        PlayerFragment.songDetailsLiveData.postValue(Pair(songTitle, artUri))
        MainFragment.songDetailsNP.postValue(Pair(songTitle, artUri))
        PlayerFragment.musicArtist.postValue(artist)

        playMusic()

        PlayerFragment.fIndex = favouriteSongChecker(PlayerFragment.musicList!![PlayerFragment.songPosition].id)
        if (PlayerFragment.isFavourite) {
            PlayerFragment.favouriteIcon.postValue(R.drawable.ic_favourite_filled)
        } else {
            PlayerFragment.favouriteIcon.postValue(R.drawable.ic_favourite_outline)
        }
    }

}