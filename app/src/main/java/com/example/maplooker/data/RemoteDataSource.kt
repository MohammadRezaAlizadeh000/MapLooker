package com.example.maplooker.data

import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.SchedulerProvider
import com.example.maplooker.data.mapper.NearLocationMapper
import com.example.maplooker.data.model.NearLocationsResponseModel
import com.example.maplooker.utils.*
import com.google.gson.Gson
import io.reactivex.rxjava3.core.Observable
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val client: OkHttpClient,
    private val scheduler: SchedulerProvider,
    private val gson: Gson
) {

    fun getNearPlaces(queries: Map<String, String>) : Observable<AppState<List<NearLocationsModel>>> {

        val result = Observable.create<AppState<List<NearLocationsModel>>> { emitter ->
            val response = client.newCall(createRequest(queries)).execute()
            if (response.isSuccessful && response.body() != null) {

                val finalData = convertJsone(response.body()?.string() ?: "")

                emitter.onNext(AppState.Success(finalData))
            } else {
                emitter.onNext(AppState.Error("خطا در شبکه"))
            }
        }.subscribeOn(scheduler.io())

        return result
    }

    private fun convertJsone(jsonString: String) : List<NearLocationsModel>{
        val finalDAta = gson.fromJson(jsonString, NearLocationsResponseModel::class.java)
        return mapData(finalDAta)
    }

    private fun mapData(data: NearLocationsResponseModel): List<NearLocationsModel> {
        return NearLocationMapper.map(data)
    }

    private fun createRequest(queries: Map<String, String>) = Request.Builder()
        .header(HEADER_KEY_QUERY, HEADER_VALUE_QUERY)
        .header(HEADER_AUTH_KEY_QUERY, AUTH_TOKEN)
        .url(createRequestUrl(queries))
        .build()

    private fun createRequestUrl(queries: Map<String, String>): String {
        val urlBuilder = HttpUrl.parse(BASE_URL)?.newBuilder()
        queries.keys.forEach { key ->
            val value = queries[key]
            urlBuilder?.addQueryParameter(key, value)
        }
        urlBuilder?.addQueryParameter("limit", "50")
        return urlBuilder?.build().toString()
    }

}