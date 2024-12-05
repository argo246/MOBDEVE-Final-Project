package com.mobdeve.s19.group2.mco2

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobdeve.s19.group2.mco2.utils.GeocodingUtils

class DetailActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.court_details)

        val courtName: TextView = findViewById(R.id.displayName)
        val courtAddress: TextView = findViewById(R.id.address)
        val courtHours: TextView = findViewById(R.id.businessHours)
        val displayImage: ImageView = findViewById(R.id.image)

        val name = intent.getStringExtra("NAME") ?: ""
        val address = intent.getStringExtra("ADDRESS") ?: ""
        val businessHours = intent.getStringExtra("BUSINESS_HOURS") ?: ""
        val imageResId = intent.getIntExtra("IMAGE", R.drawable.defaultcourt)

        courtName.text = name

        // Apply formatting with bold labels and handle new lines correctly
        courtAddress.text = formatText("Address", address)
        courtHours.text = formatText("Business Hours", businessHours)

        displayImage.setImageResource(imageResId)

        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap

            // Use the courtName for geocoding
            GeocodingUtils.getLatLngFromAddress(this, name!!) { latLng ->
                if (latLng != null) {
                    // Add a marker at the court's location and move the camera to it
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(name) // Set courtName as the marker title
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                } else {
                    // Handle error if address not found
                    Toast.makeText(this, "Location not found for $name", Toast.LENGTH_SHORT).show()
                    map.uiSettings.isScrollGesturesEnabled = false
                }
            }
        }

        val findLocationButton: Button = findViewById(R.id.findLocationButton)

        // Handle Find Location Button Click
        findLocationButton.setOnClickListener {
            // Check if courtName is not null or empty
            if (name.isNotEmpty()) {
                val uri = "geo:0,0?q=$name"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Court name is missing", Toast.LENGTH_SHORT).show()
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun formatText(label: String, value: String): CharSequence {

        return if (value.isBlank()) {
            ""
        } else {
            val formattedValue = value.replace("\n", "\n")
            val fullText = "$label: $formattedValue"
            SpannableString(fullText).apply {
                setSpan(StyleSpan(Typeface.BOLD), 0, label.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }
}