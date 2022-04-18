package com.example.maplooker.di

import androidx.lifecycle.ViewModelProvider
import com.example.maplooker.data.Repository
import com.example.maplooker.viewmodel.NearLocationViewModelFactory
import dagger.Module
import dagger.Provides

@Module
object NearLocationFragmentModule {

    @Provides
    fun provideViewModelFactory(repository: Repository): ViewModelProvider.Factory {
        return NearLocationViewModelFactory(repository)
    }

}