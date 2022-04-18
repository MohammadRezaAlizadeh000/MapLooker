package com.example.maplooker.viewmodel

import androidx.lifecycle.ViewModel
import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.data.Repository
import com.example.maplooker.utils.AppState
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class NearPlacesListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    lateinit var observer: Observable<AppState<List<NearLocationsModel>>>

    fun getNearPlaces(queries: Map<String, String>) {
        observer = repository.getUserNearPlacesFromRemote(queries)
    }

    fun getNearPlacesFromLocalData() {
        observer = repository.getUserNearPlacesFromCache()
    }

}