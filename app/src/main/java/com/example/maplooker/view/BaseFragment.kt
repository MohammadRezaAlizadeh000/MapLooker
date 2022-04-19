package com.example.maplooker.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.maplooker.utils.LocationHelper
import com.example.maplooker.utils.SchedulerProvider
import com.example.maplooker.utils.inflateLayout
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseFragment: Fragment() {

    lateinit var locationHelper: LocationHelper

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    val compositeDisposable = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        locationHelper = LocationHelper(requireContext(), requireActivity())
        return container?.inflateLayout(setView())
    }

    abstract fun setView(): Int

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