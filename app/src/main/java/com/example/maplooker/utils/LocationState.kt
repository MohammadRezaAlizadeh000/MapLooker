package com.example.maplooker.utils

import android.location.Location

sealed class LocationState(val location: Location? = null, val message: String? = null) {

    class HasLocation(location: Location): LocationState(location = location)
    class NoLocation(message: String): LocationState(message = message)
    class Error(message: String): LocationState(message = message)
}
