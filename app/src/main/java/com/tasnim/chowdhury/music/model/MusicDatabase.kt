package com.tasnim.chowdhury.music.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Music::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase: RoomDatabase() {

    abstract fun getMusicDao(): MusicDao

}