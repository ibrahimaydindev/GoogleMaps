package com.example.googlemaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.googlemaps.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.googlemaps.databinding.ActivityMapsBinding
import com.example.googlemaps.model.Place
import com.example.googlemaps.database.PlaceDao
import com.example.googlemaps.database.PlaceDatabase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private val mDisposable = CompositeDisposable()
    var selectedLatitude: Double? = null
    var selectedLongitude: Double? = null
    var placeFromMain: Place? = null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    private lateinit var sharedPreferences: SharedPreferences
    var trackBoolean: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        permission()
        selectedLatitude = 0.0
        selectedLongitude = 0.0
        binding.saveButton.isEnabled = false
        sharedPreferences =
            getSharedPreferences("com.example.googlemaps", MODE_PRIVATE)
        trackBoolean = false
        db = Room.databaseBuilder(
            applicationContext,
            PlaceDatabase::class.java, "Places"
        )
            .build()
        placeDao = db.placeDao()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        val intent = intent
        val info = intent.getStringExtra("info")
        if (info == "new") {
            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                    if (!trackBoolean!!) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                    }
                }
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //request permission
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Snackbar.make(
                        binding.root,
                        "Permission needed for location",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give Permission") {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                }
            }
        } else {
            mMap.clear()
            placeFromMain = intent.getSerializableExtra("place") as? Place
            placeFromMain?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                binding.editText.setText(it.name)
                binding.editText.isFocusable = false
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng))
        selectedLatitude = latLng.latitude
        selectedLongitude = latLng.longitude
        binding.saveButton.isEnabled = true
    }

    private fun handleResponse() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun save(view: View?) {
        val place = Place(
            binding.editText.text.toString(),
            selectedLatitude!!, selectedLongitude!!
        )
        mDisposable.add(
            placeDao.save(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    fun delete(view: View?) {
        placeFromMain?.let {
            mDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }

    private fun permission() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //permission granted
                    if (ContextCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )
                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation =
                                LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    15f
                                )
                            )
                        }
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "Permission needed!", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.clear()
    }
}









