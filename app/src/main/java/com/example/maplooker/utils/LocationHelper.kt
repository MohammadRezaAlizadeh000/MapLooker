package com.example.maplooker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.maplooker.R
import com.example.maplooker.di.DaggerAppComponent
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class LocationHelper(private val context: Context, private val rootActivity: Activity) {

    private var locationEnabled = false
    private var networkEnabled = false
    private lateinit var locationRequestBuilder: LocationSettingsRequest.Builder
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    init {
        DaggerAppComponent.create().inject(this)
    }


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
        Observable.create<List<Boolean>> { emitter ->
            val result = mutableListOf<Boolean>()
            val systemServices =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
//                locationEnabled = systemServices.isProviderEnabled(LocationManager.GPS_PROVIDER)
                result.add(systemServices.isProviderEnabled(LocationManager.GPS_PROVIDER))
            } catch (ex: Exception) {
            }

            try {
//                networkEnabled = systemServices.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                result.add(systemServices.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            } catch (ex: Exception) {
            }

            emitter.onNext(result)
        }.observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe { status ->
                locationEnabled = status[0]
                networkEnabled = status[1]
            }
        return networkEnabled && locationEnabled
    }

    fun turnOnLocation(result: (IntentSenderRequest.Builder) -> Unit) {
        Observable.create<LocationState> {
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
        }.observeOn(schedulerProvider.io())
    }

    fun getLocationPermission(result: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    rootActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
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
    fun getLastLocation(result: (LocationState) -> Unit) {
        initFusedLocation()
        Observable.create<LocationState> { emitter ->
            val taskLocation = fusedLocationProviderClient.lastLocation
            taskLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val isChanged = checkLocationChanges(location)
                    if (isChanged) {
//                        result(LocationState.HasLocation(lastLocation!!))
                        emitter.onNext(LocationState.HasLocation(lastLocation!!))
                    } else {
//                        result(LocationState.LocationNoteChange)
                        emitter.onNext(LocationState.HasLocation(lastLocation!!))
                    }
                } else {
//                    result(LocationState.NoLocation(context.getString(R.string.locationIsNull)))
                    emitter.onNext(LocationState.NoLocation(context.getString(R.string.locationIsNull)))
                }
            }

            taskLocation.addOnFailureListener { e ->
//                result(LocationState.Error(context.getString(R.string.canNotAccessLocationData)))
                emitter.onNext(LocationState.Error(context.getString(R.string.canNotAccessLocationData)))
                Log.d("LOCATION_TAG", e.toString())
            }
        }.observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe {
                result(it)
            }

    }

    private fun initFusedLocation() {
        Observable.create<Any> {
            if (!::fusedLocationProviderClient.isInitialized)
                fusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)
        }.observeOn(schedulerProvider.io())
    }

    private fun checkLocationChanges(newLocation: Location): Boolean {
        return if (lastLocation != null) {
            val distance = lastLocation!!.distanceTo(newLocation)
            if (distance > 200) {
                lastLocation = newLocation
                true
            } else
                false
        } else {
            lastLocation = newLocation
            true
        }
    }

}