package com.example.maplooker.model

data class NearLocationsModel(
    val id: String,
    val categoriesId: List<Long>,
    val latitude: String,
    val longitude: String,
    val address: String,
    val completeAddress: String,
    val country: String,
    val name: String,
)