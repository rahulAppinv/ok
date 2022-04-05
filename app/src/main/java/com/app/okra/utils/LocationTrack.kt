package com.app.okra.utils

import android.Manifest
import android.R
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*


class LocationTrack : Service(), LocationListener {
    private var count: Int = 0
    private var isFirstTime: Boolean =true
    var checkGPS = false
    private var mGPSDialog: AlertDialog? = null
    var checkNetwork = false
    var canGetLocation = false
    var loc: Location? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0

    protected var locationManager: LocationManager? = null// TODO: Consider calling

    // get network provider status
    private var location: Location? = null
    private val localBinder = LocalBinder()
    private lateinit var runnable :Runnable

    fun getLongitude(): Double {
        if (loc != null) {
            longitude = loc!!.longitude
        }
        return longitude!!
    }

    fun getLatitude(): Double {
        if (loc != null) {
            latitude = loc!!.latitude
        }
        return latitude!!
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }


    override fun onBind(intent: Intent): IBinder? {
        return localBinder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        count = 0
         runnable = Runnable {
            try {
                fetchLocationFromGPS()
            } catch (e: java.lang.Exception) {
                // TODO Auto-generated catch block
            }
        }
        getLocationData()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun getLocationData() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // get GPS status
            checkGPS = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // get network provider status
            checkNetwork = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!checkGPS && !checkNetwork) {
                // showGPSDisabledDialog(mContext,mActivity)

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(SHOW_DISABLED_GPS, true)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                //  Toast.makeText(mContext, "No Service Provider is available", Toast.LENGTH_SHORT).show();
            } else {

                canGetLocation = true

                // if GPS Enabled get lat/long using GPS Services
                if (checkGPS) {

                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                    }
                    locationManager!!.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )


                    if (locationManager != null) {
                        loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        println("::: GPS Location: $loc")

                        if (loc != null) {
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude

                            val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                            intent.putExtra(EXTRA_LOCATION, loc)
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                                intent
                            )
                        }
                    }
                } else if (checkNetwork) {
                    fetchLocationFromNetwork()
                }
                else {
                    val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                    intent.putExtra(SHOW_DISABLED_GPS, true)
                    LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//                  showGPSDisabledDialog(mContext, mActivity)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("::: Exception Location: ${e.message}")

        }
    }


    private fun fetchLocationFromGPS() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        }

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        locationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            MIN_TIME_BW_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
        )

        if (locationManager != null) {
            loc = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            println("::: GPS Location: $loc")

            if (loc != null) {
                latitude = loc!!.latitude
                longitude = loc!!.longitude

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, loc)
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }

    }


    private fun fetchLocationFromNetwork() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager!!.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            MIN_TIME_BW_UPDATES,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this
        )
        println("::: Network Location manager: $locationManager")

        if (locationManager != null) {
            loc = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        println("::: Network Location: $loc")

        if (loc != null) {
            latitude = loc!!.latitude
            longitude = loc!!.longitude

            val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, loc)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }

    }

    override fun onLocationChanged(location: Location) {
        loc= location
        if (loc != null) {
            println(":::: Fetched Location: ${loc!!.latitude}, ${loc!!.longitude}")
            latitude = loc!!.latitude
            longitude = loc!!.longitude

            val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, loc)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            locationManager?.removeUpdates(this)
        }
    }
    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}



    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1
        private const val MIN_TIME_BW_UPDATES = 2000.toLong()

        public const val TAG = "LocationTrack"

        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
        internal const val SHOW_DISABLED_GPS = "Disabled_GPS"
        internal const val AN_ERROR_OCCURRED = "Error"

    }

    init {
        // location
    }

    inner class LocalBinder : Binder() {
        internal val service: LocationTrack
            get() = this@LocationTrack
    }

    fun subscribeToLocationUpdates() {
        Log.d(TAG, "::: subscribeToLocationUpdates()")

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service, i.e., the service needs to
        // be officially started (which we do here).
        startService(Intent(applicationContext, LocationTrack::class.java))


    }

    fun unsubscribeToLocationUpdates() {
        Log.d(TAG, "::: unsubscribeToLocationUpdates()")

        stopSelf()
    }


}