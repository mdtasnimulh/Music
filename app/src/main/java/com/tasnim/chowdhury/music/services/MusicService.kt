package com.tasnim.chowdhury.music.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.utilities.Constants
import com.tasnim.chowdhury.music.utilities.getImageArt
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicService : Service() {

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    fun showNotification(playPauseBtn: Int) {

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val imageArt = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.let { getImageArt(it.path) }
        val image = if (imageArt != null) {
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        } else {
            BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
        }

        val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.title)
            .setContentText(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.artist)
            .setSmallIcon(R.drawable.ic_music)
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.ic_play, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.ic_play, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_play, "Exit", exitPendingIntent)
            //.setAutoCancel(false)
            //.setOngoing(true)
            .build()

        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (PlayerFragment.musicService?.mediaPlayer == null) {
                PlayerFragment.musicService?.mediaPlayer = MediaPlayer()
            }
            PlayerFragment.musicService?.mediaPlayer?.reset()
            PlayerFragment.musicService?.mediaPlayer?.setDataSource(PlayerFragment.musicList!![PlayerFragment.songPosition].path)
            PlayerFragment.musicService?.mediaPlayer?.prepare()
            PlayerFragment.binding.playPauseBtn.setIconResource(R.drawable.ic_pause)
            PlayerFragment.musicService?.showNotification(R.drawable.ic_pause)
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
        Log.d("PlayerFragment", "${PlayerFragment.songPosition} *-*")
    }

}