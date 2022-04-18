package com.example.maplooker.data

import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.AppState
import io.reactivex.rxjava3.core.Observable

class LocationCache {
//    var locationList: MutableList<NearLocationsModel> = mutableListOf()
    val locationList: MutableList<NearLocationsModel> = mutableListOf()

    fun getLocationsData(): Observable<AppState<List<NearLocationsModel>>> {
        return Observable.create {
            it.onNext(AppState.Success(locationList))
        }
    }
}
