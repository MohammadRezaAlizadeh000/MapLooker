package com.example.maplooker.data.model


import com.google.gson.annotations.SerializedName

data class Result(
    @SerializedName("categories")
    val categories: List<Category>?,
    @SerializedName("chains")
    val chains: List<Any>?,
    @SerializedName("distance")
    val distance: Int?,
    @SerializedName("fsq_id")
    val fsqId: String?,
    @SerializedName("geocodes")
    val geocodes: Geocodes?,
    @SerializedName("location")
    val location: Location?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("timezone")
    val timezone: String?
)