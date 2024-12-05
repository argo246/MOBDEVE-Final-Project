package com.mobdeve.s19.group2.mco2

import android.content.Context
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
import com.google.firebase.auth.FirebaseAuth
import com.mobdeve.s19.group2.mco2.utils.DatabaseHelper
import com.mobdeve.s19.group2.mco2.utils.GeocodingUtils

class GroupsDetailActivity : AppCompatActivity() {

    private lateinit var map: GoogleMap
    private lateinit var goingButton: Button
    private lateinit var goingCountText: TextView
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userId: String
    private lateinit var groupName: String
    private var isGoing = false  // To track the state of the button

    private val sharedPrefs by lazy {getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_details)

        dbHelper = DatabaseHelper(this)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""  // Get current user ID from Firebase


        val groupName: TextView = findViewById(R.id.displayName)
        val courtName: TextView = findViewById(R.id.courtName)
        val courtAddress: TextView = findViewById(R.id.address)
        val queueMaster: TextView = findViewById(R.id.queueMaster)
        val queueTime: TextView = findViewById(R.id.queueDateTime)
        val displayImage: ImageView = findViewById(R.id.Image)

        val name = intent.getStringExtra("NAME") ?: ""
        val groupCourtName = intent.getStringExtra("COURT_NAME") ?: ""
        val address = intent.getStringExtra("ADDRESS") ?: ""
        val groupQueueMaster = intent.getStringExtra("QUEUE_MASTER") ?: ""
        val queueDateTime = intent.getStringExtra("QUEUE_DATE_TIME") ?: ""
        val imageResId = intent.getIntExtra("IMAGE", R.drawable.defaultcourt)

        groupName.text = name
        this.groupName = name  // Store the group name for reference

        // Apply formatting with bold labels and handle new lines correctly
        courtName.text = formatText("Court Name", groupCourtName)
        courtAddress.text = formatText("Address", address)
        queueMaster.text = formatText("Queue Master", groupQueueMaster)
        queueTime.text = formatText("Queueing Date and Time", queueDateTime)

        displayImage.setImageResource(imageResId)

        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap

            // Use the courtName for geocoding
            GeocodingUtils.getLatLngFromAddress(this, groupCourtName!!) { latLng ->
                if (latLng != null) {
                    // Add a marker at the court's location and move the camera to it
                    map.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(groupCourtName) // Set courtName as the marker title
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                } else {
                    // Handle error if address not found
                    Toast.makeText(this, "Location not found for $groupCourtName", Toast.LENGTH_SHORT).show()
                    map.uiSettings.isScrollGesturesEnabled = false
                }
            }
        }

        val findLocationButton: Button = findViewById(R.id.findLocationButton)

        // Handle Find Location Button Click
        findLocationButton.setOnClickListener {
            // Check if courtName is not null or empty
            if (name.isNotEmpty()) {
                val uri = "geo:0,0?q=$groupCourtName"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Court name is missing", Toast.LENGTH_SHORT).show()
            }
        }

        goingButton = findViewById(R.id.goingButton)  // Button for "Going" / "Backout"
        goingCountText = findViewById(R.id.goingCountText)  // TextView for the going count
        isGoing = dbHelper.isUserGoing(name, userId)
        loadButtonState()
        setButtonState()

        goingButton.setOnClickListener {
            if (isGoing) {
                // If already going, back out
                dbHelper.updateGoingCount(name, userId, false)
                isGoing = false
            } else {
                // If not going, increment count
                dbHelper.updateGoingCount(name, userId, true)
                isGoing = true
            }
            saveButtonState()  // Save the state
            setButtonState()  // Update the button text
            updateGoingCount()  // Update the going count display
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
        // Get the initial going count for the group
        updateGoingCount()
    }

    private fun setButtonState() {
        if (isGoing) {
            goingButton.text = "Back Out"
        } else {
            goingButton.text = "Going"
        }
    }

    // Save the state of the button
    private fun saveButtonState() {
        val editor = sharedPrefs.edit()
        editor.putBoolean("isGoing_$groupName", isGoing)
        editor.apply()
    }

    // Load the saved state of the button
    private fun loadButtonState() {
        isGoing = sharedPrefs.getBoolean("isGoing_$groupName", false)
    }

    private fun updateGoingCount() {
        val count = dbHelper.getGoingCount(groupName)
        goingCountText.text = "$count people are going"
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