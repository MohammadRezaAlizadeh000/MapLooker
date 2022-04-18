package com.example.maplooker.view.adapter

import com.example.maplooker.model.NearLocationsModel

interface OnLocationClickListener {
    fun onShowMapClick(location: NearLocationsModel)
}