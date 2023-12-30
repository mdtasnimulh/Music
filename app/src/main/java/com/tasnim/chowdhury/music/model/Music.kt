package com.tasnim.chowdhury.music.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "music_table")
data class Music(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0L,
    val path: String,
    val artUri: String,
    val albumId: String,
    val dateAdded: String,
    val size: String
): Parcelable

@Parcelize
class MusicList: ArrayList<Music>(), Parcelable
