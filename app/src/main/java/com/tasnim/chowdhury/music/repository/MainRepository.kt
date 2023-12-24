package com.tasnim.chowdhury.music.repository

import android.app.Application
import android.net.Uri
import android.provider.MediaStore
import com.tasnim.chowdhury.music.model.Music
import java.io.File
import javax.inject.Inject

class MainRepository @Inject constructor() {
    /*MediaStore.Audio.Media.DATE_ADDED + " DESC"*/
    fun getAllSongs(application: Application, sortList: ArrayList<String>, position: Int): ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = application.applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortList[position],
            null
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE) ?: 0)
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID) ?: 0)
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM) ?: 0)
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) ?: 0)
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA) ?: 0)
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) ?: 0)
                    val albumIdC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) ?: 0).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        duration = durationC,
                        path = pathC,
                        artUri = artUriC,
                        albumId = albumIdC
                    )
                    val file = File(music.path)
                    if (file.exists()) {
                        tempList.add(music)
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }

}