package com.example.maplooker.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maplooker.R
import com.example.maplooker.di.DaggerAppComponent
import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.*
import com.example.maplooker.view.adapter.NearLocationsRecyclerViewAdapter
import com.example.maplooker.view.adapter.OnLocationClickListener
import com.example.maplooker.viewmodel.NearPlacesListViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NearLocationsFragment : Fragment() {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val recyclerViewAdapter: NearLocationsRecyclerViewAdapter by lazy {
        NearLocationsRecyclerViewAdapter(object : OnLocationClickListener {
            override fun onShowMapClick(location: NearLocationsModel) {
                val uri = "geo:${location.latitude}, ${location.longitude}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context!!.startActivity(intent)
//                toast(location.name)
            }
        })
    }

    private val viewModel: NearPlacesListViewModel by viewModels { viewModelFactory }
    private lateinit var recyclerView: RecyclerView
    private val compositeDisposable = CompositeDisposable()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequestBuilder: LocationSettingsRequest.Builder
    private var locationEnabled = false
    private var networkEnabled = false


    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerAppComponent.create().inject(this)
        super.onCreate(savedInstanceState)
    }

    private fun getLocation() {
        if (isLocationOn()) {
            checkLocationPermission()
        } else {
            turnOnLocation()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return container?.inflateLayout(R.layout.near_locations_fragment)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView?>(R.id.locationsRecyclerView).apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdapter
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        initFusedLocation()
        val taskLocation = fusedLocationProviderClient.lastLocation
        taskLocation.addOnSuccessListener { location ->
            if (location != null) {
                viewModel.getNearPlaces(mapOf(LL_QUERY_NAME to ("${location.latitude},${location.longitude}")))
                initObservers()
            } else {
                viewModel.getNearPlacesFromLocalData()
                toast(getString(R.string.locationIsNull))
                createBottomSheetError(LOCATION_IS_NULL)
            }
        }

        taskLocation.addOnFailureListener { e ->
            toast(getString(R.string.canNotAccessLocationData))
            Log.d("LOCATION_TAG", e.toString())
        }

    }

    private fun initObservers() {
        compositeDisposable.add(viewModel.observer.observeOn(schedulerProvider.ui()).subscribe { response ->
            when (response) {
                is AppState.Success -> {
                    toast(getString(R.string.locationDataUpdated))
                    setRecyclerViewData(response.data!!)
                }
                is AppState.Error -> toast(response.message.toString())
                is AppState.Loading -> toast(getString(R.string.loadingLocation))
            }
        })

    }

    private fun setRecyclerViewData(dataList: List<NearLocationsModel>) {
        recyclerViewAdapter.setData(dataList)
    }

    private fun turnOnLocation() {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            fastestInterval = 1500
            interval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationRequestBuilder =
            LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val result = LocationServices.getSettingsClient(requireContext())
            .checkLocationSettings(locationRequestBuilder.build())

        result.addOnCompleteListener {
            try {
                it.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        turnOnLocationRequest.launch(
                            IntentSenderRequest.Builder(
                                resolvableApiException.resolution
                            ).build()
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.d("LOCATION_TAG", e.toString())
                    } catch (e: ClassCastException) {
                        Log.d("LOCATION_TAG", e.toString())
                    }
                }
            }
        }
    }

    private val turnOnLocationRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                checkLocationPermission()
                Log.d("LOCATION_TAG", "permission Granted")
            } else {
                createBottomSheetError(LOCATION_ON_FAILED)
                Log.d("LOCATION_TAG", "permission Denied")
            }
        }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        }
    }


    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                createBottomSheetError(LOCATION_PERMISSION_NOT_GRANTED)
            }
        }

    private fun isLocationOn(): Boolean {
        val systemServices = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            getLastLocation()
        } else {
            getLocationPermission()
        }
    }

    private fun initFusedLocation() {
        if (!::fusedLocationProviderClient.isInitialized)
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun createBottomSheetError(state: Int) {
        BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {

            val bottomSheetView =
                LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null)

            bottomSheetView.findViewById<Button>(R.id.checkPermissionAgainBtn).apply {
                setOnClickListener {
                    when (state) {
                        LOCATION_ON_FAILED -> turnOnLocation()
                        LOCATION_PERMISSION_NOT_GRANTED -> getLocationPermission()
                        LOCATION_IS_NULL -> getLocation()
                    }
                    dismiss()
                }
            }

            setCancelable(false)
            setContentView(
                bottomSheetView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200.dp)
            )

            show()

        }


    }

    private fun interval() {
        compositeDisposable.add(Observable.interval(1, 1, TimeUnit.MINUTES)
            .flatMap {
                return@flatMap Observable.create<Boolean> { emitter ->
                    emitter.onNext(true)
                }
            }
            .observeOn(schedulerProvider.io())
            .subscribeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    getLocation()
//                    toast("Call again")
                    Log.d("INTERVAL_TAG", "call again")
                }
            })
    }

    override fun onStart() {
        super.onStart()
        getLocation()
        interval()
        Log.d("INTERVAL_TAG", "in OnStart")
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        Log.d("INTERVAL_TAG", "in onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}