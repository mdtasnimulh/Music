package com.tasnim.chowdhury.music.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.material.color.DynamicColors
import com.tasnim.chowdhury.music.utilities.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        val notificationChannel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = "This is a important channel for showing songs!!"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

}