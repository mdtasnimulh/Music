package com.tasnim.chowdhury.music.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MusicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMusic(music: Music)

    @Delete
    suspend fun deleteMusic(music: Music)

    @Query("SELECT * FROM music_table ORDER BY dateAdded DESC")
    fun getAllMusicSortedByDateDESC(): LiveData<List<Music>>

    @Query("SELECT * FROM music_table ORDER BY dateAdded ASC")
    fun getAllMusicSortedByDateASC(): LiveData<List<Music>>

    @Query("SELECT * FROM music_table ORDER BY title")
    fun getAllMusicSortedByTitle(): LiveData<List<Music>>

    @Query("SELECT * FROM music_table ORDER BY size DESC")
    fun getAllMusicSortedBySizeDESC(): LiveData<List<Music>>

    @Query("SELECT * FROM music_table ORDER BY size ASC")
    fun getAllMusicSortedBySizeASC(): LiveData<List<Music>>

}