package com.tasnim.chowdhury.music.viewmodel

import android.app.Application
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

    /*private val _musicList = MutableLiveData<ArrayList<Music>>()
    val musicList: LiveData<ArrayList<Music>> = _musicList

    private val _dataLoading = MutableLiveData<Boolean>().apply { value = true }
    val dataLoading: LiveData<Boolean> = _dataLoading

    // Flag to check if data has already been loaded
    private var dataLoaded = false

    fun getAllSongs(sortList: ArrayList<String>, position: Int) {
        if (!dataLoaded) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val musicList = repository.getAllSongs(application, sortList, position)
                    withContext(Dispatchers.Main) {
                        _dataLoading.value = false
                        _musicList.value = musicList
                    }
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        Log.d("chkException", "${e.message}")
                        _dataLoading.value = false
                        _musicList.value = arrayListOf()
                    }
                    Log.d("chkException", ":::${e.message}:::")
                }
            }
        }
        dataLoaded = true
    }*/

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

}