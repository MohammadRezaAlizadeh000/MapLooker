package com.example.maplooker.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.maplooker.R
import com.example.maplooker.utils.transaction
import com.example.maplooker.view.fragment.NearLocationsFragment


class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        transaction(NearLocationsFragment())

    }


}