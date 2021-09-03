package com.antoniolabra.applocation

import com.antoniolabra.applocation.databinding.ActivityMainBinding
import com.antoniolabra.applocation.helpers.getAppPermissions
import com.antoniolabra.applocation.helpers.openAppPermissionsScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.araujo.jordan.excuseme.ExcuseMe
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val locationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var stopService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        requestPermissions()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Este botón inicia con getLocation y este a su vez el listener
        binding.btnHelp.setOnClickListener {
            stopService = false
            binding.btnStop.visibility = View.VISIBLE
            binding.btnHelp.visibility = View.GONE
            validatePermissionInRuntime()
        }

        // Este botón detiene listener que desata getLocation
        binding.btnStop.setOnClickListener {
            binding.btnStop.visibility = View.GONE
            binding.btnHelp.visibility = View.VISIBLE
            stopService = true
        }
    }

    // Valida que se tengan los permisos para continuar o manda a Settings
    private fun validatePermissionInRuntime(){
        ExcuseMe.couldYouGive(this).permissionFor(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) {
            if (it.granted.contains(Manifest.permission.ACCESS_FINE_LOCATION) && it.granted.contains(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                getLocation()
            } else {

                Snackbar.make(View(this@MainActivity), "Necesitas darnos permiso",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Abrir Permisos") {
                    openAppPermissionsScreen(this)
                }
            }
        }
    }

    // Se piden los permisos por primera vez
    private fun requestPermissions() {
        ExcuseMe.couldYouGive(this).permissionFor(*getAppPermissions()) {
            val message = if (it.denied.size > 0) R.string.denied_permissions_message_hint else R.string.granted_permissions_message_hint
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    //Este es el problemático jeje
    @SuppressLint("MissingPermission")
    private fun getLocation() {

        try {

            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (hasGps) {

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000,
                    0.5f,
                    object : LocationListener {

                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                            Log.d("GPS", "$status")
                        }

                        override fun onLocationChanged(mLocation: Location) {
                            Toast.makeText(
                                applicationContext,
                                "${mLocation.latitude}, ${mLocation.longitude}",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Aquí se detiene el listener
                            if (stopService) {
                                locationManager.removeUpdates(this)
                            }
                        }

                        override fun onProviderEnabled(message: String) {
                            Log.d("GPS", message)
                        }

                        override fun onProviderDisabled(message: String) {
                            Log.d("GPS", message)
                        }

                        //Pd. le puse todos los otros override porque leí por ahí que eran obligatorios. onLocationChanged es el único que uso realmente
                    })


            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } catch (e: Exception) {
            Log.d("GPS", e.message.toString())
        }

    }
}