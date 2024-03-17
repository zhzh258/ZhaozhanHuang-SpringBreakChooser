package com.example.springbreakchooser

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.springbreakchooser.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mediaPlayer: MediaPlayer;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val latitude = intent.getDoubleExtra("latitude", -34.0) // Default to Sydney if not found
        val longitude = intent.getDoubleExtra("longitude", 151.0) // Default to Sydney if not found
        val markerTitle = intent.getStringExtra("title") ?: "Default Title"
        val languageCode = intent.getStringExtra("language") ?: "en"

        // Add a marker in Sydney and move the camera
        val cityLatLng = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(cityLatLng).title(markerTitle)).also {
            it?.showInfoWindow()
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cityLatLng))
        playGreeting(languageCode)
    }

    private fun playGreeting(languageCode: String) {
        val audioResourceId = when (languageCode) {
            "es" -> R.raw.spanish
            "fr" -> R.raw.french
            "zh" -> R.raw.mandarin
            else -> R.raw.spanish // Default to Spanish
        }
        mediaPlayer = MediaPlayer.create(this, audioResourceId)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }
}