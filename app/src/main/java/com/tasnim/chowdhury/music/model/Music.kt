package com.tasnim.chowdhury.music.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Music(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String,
): Parcelable

@Parcelize
class MusicList: ArrayList<Music>(), Parcelable
