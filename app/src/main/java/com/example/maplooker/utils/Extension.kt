package com.example.maplooker.utils

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

val Number.dp get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics).toInt()

fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun FragmentActivity.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


fun ViewGroup.inflateLayout(resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

fun FragmentActivity.transaction(fragment: Fragment) {
    supportFragmentManager.beginTransaction().replace(FRAGMENT_HOST, fragment)
        .addToBackStack(null)
        .commit()
}