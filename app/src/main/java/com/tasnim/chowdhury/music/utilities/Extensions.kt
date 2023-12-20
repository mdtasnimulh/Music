package com.tasnim.chowdhury.music.utilities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Service
import android.media.MediaMetadataRetriever
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.ui.fragments.FavouritesFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import java.io.File
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

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
    playlist.forEachIndexed{ index, music ->
        val file = File(music.path)
        if (!file.exists()) {
            playlist.removeAt(index)
        }
    }
    return playlist
}


fun View.animateRotation(rotation: Float, duration: Long) {
    val rotateAnimator = ObjectAnimator.ofFloat(this, View.ROTATION, rotation)
    rotateAnimator.duration = duration
    rotateAnimator.start()
}

fun View.animateElevation(elevation: Float, duration: Long) {
    val elevateAnimator = ObjectAnimator.ofFloat(this, View.TRANSLATION_Z, elevation)
    elevateAnimator.duration = duration
    elevateAnimator.start()
}

fun View.animateMargins(
    startMarginEnd: Int, endMarginEnd: Int,
    startMarginTop: Int, endMarginTop: Int,
    duration: Long
) {
    val marginAnimator = ValueAnimator.ofFloat(0f, 1f)
    marginAnimator.duration = duration
    marginAnimator.addUpdateListener { valueAnimator ->
        val fraction = valueAnimator.animatedValue as Float
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.marginEnd = (startMarginEnd + fraction * (endMarginEnd - startMarginEnd)).toInt()
        layoutParams.topMargin = (startMarginTop + fraction * (endMarginTop - startMarginTop)).toInt()
        this.layoutParams = layoutParams
    }
    marginAnimator.start()
}
