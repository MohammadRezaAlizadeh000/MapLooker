package com.example.maplooker.view.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.maplooker.R
import com.example.maplooker.model.NearLocationsModel
import com.example.maplooker.utils.inflateLayout

class NearLocationsRecyclerViewAdapter(private val clickListener: OnLocationClickListener) :
    RecyclerView.Adapter<NearLocationsRecyclerViewAdapter.NearLocationHolder>() {

    private val dataList = mutableListOf<NearLocationsModel>()
    fun setData(data: List<NearLocationsModel>) {
        dataList.apply {
            clear()
            addAll(data)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearLocationHolder {
        return NearLocationHolder(parent.inflateLayout(R.layout.locations_recyclerview_row)).apply {
            showLocationOnMapBtn.setOnClickListener {
                val location = dataList[layoutPosition]
                clickListener.onShowMapClick(location)
            }
        }
    }

    override fun onBindViewHolder(holder: NearLocationHolder, position: Int) {
        val model = dataList[position]
        with(holder) {
            locationName.text = model.name
            locationAddress.text = model.completeAddress
        }
    }

    override fun getItemCount() = dataList.size

    class NearLocationHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationName: TextView = itemView.findViewById(R.id.locationName)
        val locationAddress: TextView = itemView.findViewById(R.id.locationAddress)
        val showLocationOnMapBtn: TextView = itemView.findViewById(R.id.showLocationOnMapBtn)
    }


}