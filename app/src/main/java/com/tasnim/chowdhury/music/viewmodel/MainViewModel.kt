package com.tasnim.chowdhury.music.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.repository.MainRepository
import com.tasnim.chowdhury.music.utilities.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val application: Application
) : BaseViewModel(application){

    private val musicSortedByDateDesc = repository.getAllMusicSortedByDateDESC()
    private val musicSortedByDateAsc = repository.getAllMusicSortedByDateASC()
    private val musicSortedByTitle = repository.getAllMusicSortedByTitle()
    private val musicSortedBySizeDesc = repository.getAllMusicSortedBySizeDESC()
    private val musicSortedBySizeAsc = repository.getAllMusicSortedBySizeASC()

    val musics = MediatorLiveData<List<Music>>()
    var sortType = SortType.DATE_DESC

    init {
        musics.addSource(musicSortedByDateDesc) { music ->
            if (sortType == SortType.DATE_DESC) {
                music?.let { musics.value = it }
            }
        }
        musics.addSource(musicSortedByDateAsc) { music ->
            if (sortType == SortType.DATE_ASC) {
                music?.let { musics.value = it }
            }
        }
        musics.addSource(musicSortedByTitle) { music ->
            if (sortType == SortType.TITLE) {
                music?.let { musics.value = it }
            }
        }
        musics.addSource(musicSortedBySizeDesc) { music ->
            if (sortType == SortType.SIZE_DESC) {
                music?.let { musics.value = it }
            }
        }
        musics.addSource(musicSortedBySizeAsc) { music ->
            if (sortType == SortType.SIZE_ASC) {
                music?.let { musics.value = it }
            }
        }
    }

    fun sortMusic(sortType: SortType) = when(sortType) {
        SortType.DATE_DESC -> musicSortedByDateDesc.value?.let { musics.value = it }
        SortType.DATE_ASC -> musicSortedByDateAsc.value?.let { musics.value = it }
        SortType.TITLE -> musicSortedByTitle.value?.let { musics.value = it }
        SortType.SIZE_DESC -> musicSortedBySizeDesc.value?.let { musics.value = it }
        SortType.SIZE_ASC -> musicSortedBySizeAsc.value?.let { musics.value = it }
    }.also {
        this.sortType = sortType
    }

    fun insertMusic(music: Music) = viewModelScope.launch {
        repository.insertMusic(music)
    }

    fun deleteMusic(position: Int, music: Music) = viewModelScope.launch {
        try {
            Log.d("FilePath", "Deleting file: ${music.path}")
            repository.deleteMusic(music)

            musics.value = musics.value?.toMutableList()?.apply {
                removeAt(position)
            }
        } catch (e: Exception) {
            Log.d("CatchException", ":::${e.message}:::")
            e.printStackTrace()
        }
    }

}