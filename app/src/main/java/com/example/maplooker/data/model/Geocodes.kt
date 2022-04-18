package com.example.maplooker.data.model


import com.google.gson.annotations.SerializedName

data class Geocodes(
    @SerializedName("main")
    val main: Main?
)