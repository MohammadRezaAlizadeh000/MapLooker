package com.example.maplooker.view.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maplooker.R
import com.example.maplooker.di.DaggerAppComponent
import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.*
import com.example.maplooker.view.BaseFragment
import com.example.maplooker.view.adapter.NearLocationsRecyclerViewAdapter
import com.example.maplooker.view.adapter.OnLocationClickListener
import com.example.maplooker.viewmodel.NearPlacesListViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class NearLocationsFragment : BaseFragment() {

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

    private val turnOnLocationRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                locationPermission()
                Log.d("LOCATION_TAG", "permission Granted")
            } else {
                createBottomSheetError(LOCATION_ON_FAILED)
                Log.d("LOCATION_TAG", "permission Denied")
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
    private val viewModel: NearPlacesListViewModel by viewModels { viewModelFactory }
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerAppComponent.create().inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun setView() = R.layout.near_locations_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView = view.findViewById<RecyclerView?>(R.id.locationsRecyclerView).apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = recyclerViewAdapter
        }
    }

    private fun checkLocationStatus() {
        if (locationHelper.isLocationOn()) {
            locationPermission()
        } else {
            turnOnLocation()
        }
    }

    private fun turnOnLocation() {
        locationHelper.turnOnLocation { turnOnLocationRequest.launch(it.build()) }
    }



    private fun locationPermission() {
        locationHelper.getLocationPermission {
            when(it) {
                "Granted" -> getLastLocation()
                else -> locationPermission.launch(it)
            }
        }
    }

    private fun getLastLocation() {
        locationHelper.getLastLocation {
            when(it) {
                is LocationState.HasLocation -> {
                    viewModel.getNearPlaces(mapOf(LL_QUERY_NAME to ("${it.location!!.latitude},${it.location.longitude}")))
                    initObservers()
                }
                is LocationState.NoLocation -> {
                    viewModel.getNearPlacesFromLocalData()
                    toast(it.message.toString())
                    createBottomSheetError(LOCATION_IS_NULL)
                }
                is LocationState.Error -> {
                    toast(it.message.toString())
                }
            }
        }
    }



    private fun initObservers() {
        compositeDisposable.add(viewModel.observer.observeOn(schedulerProvider.ui()).subscribe { response ->
            when (response) {
                is AppState.Success -> {
                    toast(getString(R.string.locationDataUpdated))
                    recyclerViewAdapter.setData(response.data!!)
                }
                is AppState.Error -> toast(response.message.toString())
                is AppState.Loading -> toast(getString(R.string.loadingLocation))
            }
        })

    }

    private fun createBottomSheetError(state: Int) {
        BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme).apply {

            val bottomSheetView =
                LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null)

            bottomSheetView.findViewById<Button>(R.id.checkPermissionAgainBtn).apply {
                setOnClickListener {
                    when (state) {
                        LOCATION_ON_FAILED -> turnOnLocation()
                        LOCATION_PERMISSION_NOT_GRANTED -> locationPermission()
                        LOCATION_IS_NULL -> checkLocationStatus()
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
                    checkLocationStatus()
//                    toast("Call again")
                    Log.d("INTERVAL_TAG", "call again")
                }
            })
    }

    override fun onStart() {
        super.onStart()
        checkLocationStatus()
        interval()
        Log.d("INTERVAL_TAG", "in OnStart")
    }
}