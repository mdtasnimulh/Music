package com.tasnim.chowdhury.music.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.ui.MainActivity
import com.tasnim.chowdhury.music.ui.fragments.MainFragment
import com.tasnim.chowdhury.music.ui.fragments.PlayerFragment
import com.tasnim.chowdhury.music.utilities.Constants
import com.tasnim.chowdhury.music.utilities.Constants.NOTIFICATION_CHANNEL_ID
import com.tasnim.chowdhury.music.utilities.Constants.NOTIFICATION_CHANNEL_NAME
import com.tasnim.chowdhury.music.utilities.Constants.NOTIFICATION_ID
import com.tasnim.chowdhury.music.utilities.getImageArt
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var runnable: Runnable

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    lateinit var audioManager: AudioManager

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
    fun showNotification(playPauseBtn: Int, playPauseBlack: Int, playBackSpeed: Float) {

        val intent = Intent(baseContext, MainActivity::class.java)
        val playerIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)

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
            BitmapFactory.decodeResource(baseContext.resources, R.drawable.ic_launcher_foreground)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        notificationBuilder.mActions.clear()

        val notificationBuilder = notificationBuilder
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setContentTitle(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.title)
            .setContentText(PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.artist)
            .setLargeIcon(image)
            .addAction(R.drawable.ic_back, "Previous", prevPendingIntent)
            .addAction(playPauseBlack, "Play", playPendingIntent)
            .addAction(R.drawable.ic_forward, "Next", nextPendingIntent)
            .addAction(R.drawable.ic_close, "Exit", exitPendingIntent)
            .setContentIntent(playerIntent)

        /*mediaSession.setMetadata(mediaPlayer?.duration?.toLong()?.let {
            MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, it)
                .build()
        })
        mediaSession.setPlaybackState(mediaPlayer?.currentPosition?.toLong()?.let {
            PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, it, playBackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
        })*/

        PlayerFragment.playPauseIconLiveData.postValue(playPauseBtn)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun createMediaPlayer() {
        try {
            if (PlayerFragment.musicService?.mediaPlayer == null) {
                PlayerFragment.musicService?.mediaPlayer = MediaPlayer()
            }
            PlayerFragment.musicService?.mediaPlayer?.reset()
            PlayerFragment.musicService?.mediaPlayer?.setDataSource(PlayerFragment.musicList!![PlayerFragment.songPosition].path)
            PlayerFragment.musicService?.mediaPlayer?.prepare()
            PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 0F)
            PlayerFragment.startTimeLiveData.postValue(mediaPlayer?.currentPosition?.toLong())
            PlayerFragment.endTimeLiveData.postValue(mediaPlayer?.duration?.toLong())
            PlayerFragment.initialProgressLiveData.postValue(0)
            PlayerFragment.progressMaxLiveData.postValue(mediaPlayer?.duration)
            PlayerFragment.nowPlayingId = PlayerFragment.musicList?.get(PlayerFragment.songPosition)?.id.toString()
            PlayerFragment.animateDisk.postValue("")
        }catch (e: Exception) {
            Log.d("chkException", "Exception:::${e.message}")
        }
    }

    fun seekBarSetup() {
        runnable = Runnable {
            PlayerFragment.startTimeLiveData.postValue(mediaPlayer?.currentPosition?.toLong())
            PlayerFragment.initialProgressLiveData.postValue(mediaPlayer?.currentPosition)
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            //pause music
            PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_play)
            MainFragment.playPauseIconNP.postValue(R.drawable.ic_player_play)
            PlayerFragment.musicService?.showNotification(R.drawable.ic_player_play, R.drawable.ic_play, 0F)
            PlayerFragment.isPlaying = false
            PlayerFragment.musicService?.mediaPlayer?.pause()
        } else {
            //play music
            PlayerFragment.playPauseIconLiveData.postValue(R.drawable.ic_player_pause)
            MainFragment.playPauseIconNP.postValue(R.drawable.ic_player_pause)
            PlayerFragment.musicService?.showNotification(R.drawable.ic_player_pause, R.drawable.ic_pause, 1F)
            PlayerFragment.isPlaying = true
            PlayerFragment.musicService?.mediaPlayer?.start()
        }
    }

}