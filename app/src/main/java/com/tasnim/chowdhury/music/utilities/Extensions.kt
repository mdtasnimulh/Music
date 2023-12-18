package com.tasnim.chowdhury.music.utilities

import android.app.Service
import android.media.MediaMetadataRetriever
import com.tasnim.chowdhury.music.ui.fragments.FavouritesFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            (minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerFragment.repeat) {
        if (increment) {
            if ((PlayerFragment.musicList?.size?.minus(1) ?: 0) == PlayerFragment.songPosition) {
                PlayerFragment.songPosition = 0
            } else {
                ++PlayerFragment.songPosition
            }
        } else {
            if (PlayerFragment.songPosition == 0) {
                PlayerFragment.songPosition = PlayerFragment.musicList?.size?.minus(1) ?: 0
            } else {
                --PlayerFragment.songPosition
            }
        }
    }
}

fun closeApp(){
    if (PlayerFragment.musicService != null) {
        PlayerFragment.musicService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        PlayerFragment.musicService?.mediaPlayer?.release()
        PlayerFragment.musicService = null
    }
    exitProcess(1)
}

fun favouriteSongChecker(id: String): Int {
    PlayerFragment.isFavourite = false
    FavouritesFragment.favouriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerFragment.isFavourite = true
            return index
        }
    }
    return -1
}