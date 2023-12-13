package com.tasnim.chowdhury.music.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.utilities.Constants
import com.tasnim.chowdhury.music.utilities.formatDuration
import com.tasnim.chowdhury.music.utilities.getImageArt
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service() {

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManagerCompat

    private lateinit var runnable: Runnable

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

    @SuppressLint("RestrictedApi")
    fun showNotification(playPauseBtn: Int) {

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(Constants.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        Log.d("hello", "showNotification: hello $prevIntent")

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
            BitmapFactory.decodeResource(baseContext.resources, R.drawable.ic_launcher_foreground)
        }

        notificationBuilder.mActions.clear()

        val notificationBuilder = notificationBuilder
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setContentTitle(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.title)
            .setContentText(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.artist)
            .setLargeIcon(image)
            .addAction(R.drawable.ic_back, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.ic_forward, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_close, "Exit", exitPendingIntent)

        PlayerFragment.playPauseIconLiveData.postValue(playPauseBtn)

        startForeground(
            13, notificationBuilder.build())
    }

    fun createMediaPlayer() {
        try {
            if (PlayerFragment.musicService?.mediaPlayer == null) {
                PlayerFragment.musicService?.mediaPlayer = MediaPlayer()
            }
            PlayerFragment.musicService?.mediaPlayer?.reset()
            PlayerFragment.musicService?.mediaPlayer?.setDataSource(PlayerFragment.musicList!![PlayerFragment.songPosition].path)
            PlayerFragment.musicService?.mediaPlayer?.prepare()
            PlayerFragment.musicService?.showNotification(R.drawable.ic_pause)
            PlayerFragment.startTimeLiveData.postValue(mediaPlayer?.currentPosition?.toLong())
            PlayerFragment.endTimeLiveData.postValue(mediaPlayer?.duration?.toLong())
            PlayerFragment.initialProgressLiveData.postValue(0)
            PlayerFragment.progressMaxLiveData.postValue(mediaPlayer?.duration)
            Log.d("hello", "Exception:::hello1")
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
        Log.d("PlayerFragment", "${PlayerFragment.songPosition} *-*")
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerFragment.startTimeLiveData.postValue(mediaPlayer?.currentPosition?.toLong())
            PlayerFragment.initialProgressLiveData.postValue(mediaPlayer?.currentPosition)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

}