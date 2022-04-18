package com.example.maplooker.data

import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.AppState
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class Repository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocationCache
) {

    fun getUserNearPlacesFromRemote(queries: Map<String, String>): Observable<AppState<List<NearLocationsModel>>> {
        val requestResult = remoteDataSource.getNearPlaces(queries)
        requestResult.subscribe {
            when (it) {
                is AppState.Success -> { addDataToCache(it.data!!) }
                is AppState.Error -> {  }
                is AppState.Loading -> {  }
            }
        }
        return requestResult
    }

    private fun addDataToCache(data: List<NearLocationsModel>) {
        localDataSource.locationList.apply {
            clear()
            addAll(data)
        }
    }

    fun getUserNearPlacesFromCache(): Observable<AppState<List<NearLocationsModel>>> {
        return localDataSource.getLocationsData()
    }

}