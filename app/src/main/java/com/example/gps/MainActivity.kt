package com.example.gps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resultTextView: TextView

    private var trackingStarted = false
    private var previousLocation: android.location.Location? = null
    private var distance = 0.0
    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startButton = findViewById(R.id.start_button)
        stopButton = findViewById(R.id.stop_button)
        resultTextView = findViewById(R.id.result_text_view)

        startButton.setOnClickListener { startTracking() }
        stopButton.setOnClickListener { stopTracking() }
    }

    private fun startTracking() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            trackingStarted = true
            startTime = System.currentTimeMillis()
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                previousLocation = location
            }
        }
    }

    private fun stopTracking() {
        trackingStarted = false
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0 // en secondes
        resultTextView.text = "Distance parcourue : $distance m\nTemps écoulé : $elapsedTime s"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startTracking()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 100
    }

    private fun calculateDistance(
        previousLocation: android.location.Location?,
        newLocation: android.location.Location
    ): Double {
        if (previousLocation == null) {
            return 0.0
        }
        val lat1 = previousLocation.latitude
        val lon1 = previousLocation.longitude
        val lat2 = newLocation.latitude
        val lon2 = newLocation.longitude
        val earthRadius = 6371e3 // en mètres
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2))
        val c = 2 * Math.atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
