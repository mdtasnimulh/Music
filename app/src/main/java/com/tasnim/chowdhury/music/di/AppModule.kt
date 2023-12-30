package com.tasnim.chowdhury.music.di

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.tasnim.chowdhury.music.R
import com.tasnim.chowdhury.music.model.MusicDatabase
import com.tasnim.chowdhury.music.utilities.Constants
import com.tasnim.chowdhury.music.utilities.Constants.MUSIC_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
    ) : NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
    }

    @Singleton
    @Provides
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        MusicDatabase::class.java,
        MUSIC_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideMusicDao(db: MusicDatabase) = db.getMusicDao()

}