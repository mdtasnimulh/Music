package com.tasnim.chowdhury.music.utilities

import android.media.MediaMetadataRetriever
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import java.util.concurrent.TimeUnit

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