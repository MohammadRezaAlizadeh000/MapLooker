package com.example.maplooker.data.mapper

import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.data.model.NearLocationsResponseModel

object NearLocationMapper: BaseMapper<NearLocationsResponseModel, List<NearLocationsModel>> {

    override fun map(data: NearLocationsResponseModel): List<NearLocationsModel> {
        return data.results?.map { result ->
            NearLocationsModel(
                result.fsqId!!,
                result.categories?.map { category -> category.id!!.toLong() } ?: listOf(),
                result.geocodes?.main?.latitude.toString(),
                result.geocodes?.main?.longitude.toString(),
                result.location?.address.toString(),
                result.location?.formattedAddress.toString(),
                result.location?.country.toString(),
                result.name.toString()
            )
        } ?: listOf()

    }

}