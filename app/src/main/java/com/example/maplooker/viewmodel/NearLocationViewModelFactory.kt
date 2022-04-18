package com.example.maplooker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.maplooker.data.Repository
import javax.inject.Singleton

@Singleton
class NearLocationViewModelFactory(private val repository: Repository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return NearPlacesListViewModel(repository) as T
    }
}