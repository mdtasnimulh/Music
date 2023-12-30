package com.tasnim.chowdhury.music.repository

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.model.MusicDao
import java.io.File
import javax.inject.Inject

class MainRepository @Inject constructor(
    val musicDao: MusicDao
) {

    suspend fun insertMusic(music: Music) = musicDao.insertMusic(music)

    suspend fun deleteMusic(music: Music) = musicDao.deleteMusic(music)

    fun getAllMusicSortedByDateDESC() = musicDao.getAllMusicSortedByDateDESC()

    fun getAllMusicSortedByDateASC() = musicDao.getAllMusicSortedByDateASC()

    fun getAllMusicSortedByTitle() = musicDao.getAllMusicSortedByTitle()

    fun getAllMusicSortedBySizeDESC() = musicDao.getAllMusicSortedBySizeDESC()

    fun getAllMusicSortedBySizeASC() = musicDao.getAllMusicSortedBySizeASC()


}