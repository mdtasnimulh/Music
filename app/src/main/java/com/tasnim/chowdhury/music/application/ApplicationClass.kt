package com.tasnim.chowdhury.music.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.MediaCas.Session
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.material.color.DynamicColors
import com.tasnim.chowdhury.music.utilities.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ApplicationClass: Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

}