package com.mobdeve.s19.group2.mco2.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

object GeocodingUtils {

    // Use this method to get the LatLng for a given address
    fun getLatLngFromAddress(context: Context, address: String, callback: (LatLng?) -> Unit) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                val location = results[0]
                callback(LatLng(location.latitude, location.longitude))
            } else {
                callback(null) // No result
            }
        } catch (e: Exception) {
            Log.e("GeocodingUtils", "Error getting LatLng from address: ${e.localizedMessage}")
            callback(null)
        }
    }
}