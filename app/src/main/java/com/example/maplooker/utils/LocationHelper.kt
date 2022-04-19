package com.example.maplooker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maplooker.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationHelper(private val context: Context, private val rootActivity: Activity) {

    private var locationEnabled = false
    private var networkEnabled = false
    private lateinit var locationRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    /*
    *
    *
    * checkLocation is On ot Off
    *   On -> checkLocationPermission is Granted or Denied
    *       Granted -> get last location is Null Success Error
    *           Null -> toast and show retry btn
    *           Success -> update ui
    *           Error -> toast and show retry btn
    *       Denied -> show error
    *   Off -> ask to turn on location is Granted or Denied
    *       Granted -> checkLocationPermission is Granted or Denied
    *           Granted -> get last location is Null Success Error
    *               Null -> toast and show retry btn
    *               Success -> update ui
    *               Error -> toast and show retry btn
    *           Denied -> show error
    *       Denied -> show error
    *
    * */


    fun isLocationOn(): Boolean {
        val systemServices = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationEnabled = systemServices.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            networkEnabled = systemServices.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }


        return networkEnabled && locationEnabled
    }

    fun turnOnLocation(result: (IntentSenderRequest.Builder) -> Unit) {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            fastestInterval = 1500
            interval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result = LocationServices.getSettingsClient(context)
            .checkLocationSettings(locationRequestBuilder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        result(IntentSenderRequest.Builder(resolvableApiException.resolution))
                    } catch (e: IntentSender.SendIntentException) {
                        Log.d("LOCATION_TAG", e.toString())
                    } catch (e: ClassCastException) {
                        Log.d("LOCATION_TAG", e.toString())
                    }
                }
            }
        }
    }

    fun getLocationPermission(result: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(rootActivity, Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                result(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                result(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        } else {
            result("Grated")
        }
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(result : (LocationState) -> Unit) {
        initFusedLocation()
        val taskLocation = fusedLocationProviderClient.lastLocation
        taskLocation.addOnSuccessListener { location ->
            if (location != null) {
                result(LocationState.HasLocation(location))
            } else {
                result(LocationState.NoLocation(context.getString(R.string.locationIsNull)))
            }
        }

        taskLocation.addOnFailureListener { e ->
            result(LocationState.Error(context.getString(R.string.canNotAccessLocationData)))
            Log.d("LOCATION_TAG", e.toString())
        }

    }

    private fun initFusedLocation() {
        if (!::fusedLocationProviderClient.isInitialized)
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)
    }

}