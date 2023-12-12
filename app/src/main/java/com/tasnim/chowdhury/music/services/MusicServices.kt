package com.tasnim.chowdhury.music.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicServices : Service() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null

    companion object {

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onBind(p0: Intent?): IBinder? {
        return myBinder
    }

    inner class MyBinder: Binder() {
        fun currentService(): MusicServices {
            return this@MusicServices
        }
    }

}