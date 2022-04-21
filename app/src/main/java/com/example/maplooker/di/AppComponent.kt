package com.example.maplooker.di

import com.example.maplooker.utils.LocationHelper
import com.example.maplooker.view.fragment.NearLocationsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, NearLocationFragmentModule::class, NetworkModule::class])
interface AppComponent {
    fun inject(f: NearLocationsFragment)

    fun inject(lh: LocationHelper)
}