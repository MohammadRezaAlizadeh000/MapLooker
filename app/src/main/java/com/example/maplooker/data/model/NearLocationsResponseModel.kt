package com.example.maplooker.data.model


import com.google.gson.annotations.SerializedName

data class NearLocationsResponseModel(
    @SerializedName("results")
    val results: List<Result>?
)