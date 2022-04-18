package com.example.maplooker.data.model


import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("address")
    val address: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("cross_street")
    val crossStreet: String?,
    @SerializedName("formatted_address")
    val formattedAddress: String?,
    @SerializedName("locality")
    val locality: String?,
    @SerializedName("postcode")
    val postcode: String?,
    @SerializedName("region")
    val region: String?
)