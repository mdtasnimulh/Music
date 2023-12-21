package com.tasnim.chowdhury.music.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tasnim.chowdhury.music.model.Music
import com.tasnim.chowdhury.music.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository,
    private val application: Application
) : BaseViewModel(application){

    private val _musicList = MutableLiveData<ArrayList<Music>>()
    val musicList: LiveData<ArrayList<Music>> = _musicList

    private val _dataLoading = MutableLiveData<Boolean>().apply { value = true }
    val dataLoading: LiveData<Boolean> = _dataLoading

    fun getAllSongs(sortList: ArrayList<String>, position: Int) {
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

}