package com.farhan.rideshare.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.farhan.rideshare.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.MapView
import com.farhan.rideshare.utils.Constant.MAPVIEW_BUNDLE_KEY
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import android.view.View
import androidx.core.content.ContextCompat
import com.farhan.rideshare.directionhelpers.FetchURL
import com.farhan.rideshare.directionhelpers.TaskLoadedCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.sheet_map.*
import com.google.maps.GeoApiContext
import com.google.maps.model.DirectionsResult
import com.google.maps.DirectionsApiRequest
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding


class DriverActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener, TaskLoadedCallback {

    private val TAG = "DriverActivity"
    private var mMapView: MapView? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mGoogleMap: GoogleMap? = null
    private var mMapBoundary: LatLngBounds? = null
    private var pickUpBool:Boolean = false
    private var dropOffBool:Boolean = false
    private var pickUpMarker: MarkerOptions = MarkerOptions()
    private var dropOffMarker: MarkerOptions = MarkerOptions()
    private var firstTimeCheckPickUp = false
    private var firstTimeCheckDropOff = false
    private var mGeoApiContext: GeoApiContext? = null
    private var currentPolyline:Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver)
        mMapView = findViewById(R.id.map_view)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initGoogleMap(savedInstanceState)

        btn_pick_up.setOnClickListener(this)
        btn_drop_off.setOnClickListener(this)
        btn_calc_distance.setOnClickListener(this)
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMapView?.onCreate(mapViewBundle)
        mMapView?.getMapAsync(this)
        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                    .apiKey("AIzaSyAyVirgyaPoZXHrNq1NSd1FXoxsgnl5_Hc")
                    .build()
        }
    }

    private fun calculateDirections(originMarker:MarkerOptions,destinationMarker: MarkerOptions) {
        Log.e(TAG, "calculateDirections: calculating directions.")

        val destination = com.google.maps.model.LatLng(
                destinationMarker.position.latitude,
                destinationMarker.position.longitude
        )
        val directions = DirectionsApiRequest(mGeoApiContext)

        directions.alternatives(true)
        directions.origin(
                com.google.maps.model.LatLng(
                        originMarker.position.latitude,
                        originMarker.position.longitude
                )
        )
        Log.e(TAG, "calculateDirections: destination: $destination")
        directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult>{
           override fun onResult(result: DirectionsResult) {
                Log.e(TAG, "onResult: routes: " + result.routes[0].toString())
                Log.e(TAG, "onResult: geocodedWayPoints: " + result.geocodedWaypoints[0].toString())
               addPolyLinesToMap(result)
            }

            override  fun onFailure(e: Throwable) {
                Log.e(TAG, "onFailure: " + e.message)
            }
        })
    }

    private fun addPolyLinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "run: result routes: " + result.routes.size)

            for (route in result.routes) {
                Log.d(TAG, "run: leg: " + route.legs[0].toString())
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)

                val newDecodedPath = ArrayList<LatLng>()

                // This loops through all the LatLng coordinates of ONE polyline.
                for (latLng in decodedPath) {

                    //                        Log.d(TAG, "run: latlng: " + latLng.toString());

                    newDecodedPath.add(LatLng(
                            latLng.lat,
                            latLng.lng
                    ))
                }
                val polyline = mGoogleMap?.addPolyline(PolylineOptions().addAll(newDecodedPath))
                polyline?.color = ContextCompat.getColor(this@DriverActivity, R.color.colorAccent)
                polyline?.isClickable = true

            }
        }
    }

    private fun getLastKnownLocation() {
        Log.e(TAG, "getLastKnownLocation: called.")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mFusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result
                setCameraView(location!!.latitude,location.longitude)
            }
        }

    }

    private fun setCameraView(lat:Double,lang:Double) {
        // Set a boundary to start
        val bottomBoundary = lat - .1
        val leftBoundary =lang - .1
        val topBoundary = lat + .1
        val rightBoundary = lang + .1

        mMapBoundary = LatLngBounds(
                LatLng(bottomBoundary, leftBoundary),
                LatLng(topBoundary, rightBoundary)
        )

        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0))
    }


    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mMapView?.onSaveInstanceState(mapViewBundle)
    }

    public override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    public override fun onStart() {
        super.onStart()
        mMapView?.onStart()
    }

    public override fun onStop() {
        super.onStop()
        mMapView?.onStop()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(this@DriverActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@DriverActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        map.isMyLocationEnabled = true
        mGoogleMap = map
        getLastKnownLocation()

        mGoogleMap?.setOnMapClickListener {latLng->
            when {
                pickUpBool -> {
                    pickUpMarker.position(latLng)
                    mGoogleMap?.addMarker(pickUpMarker)

                    if (firstTimeCheckDropOff){
                        mGoogleMap?.clear()
                        mGoogleMap?.addMarker(pickUpMarker)
                        mGoogleMap?.addMarker(dropOffMarker)
                    }else{
                        mGoogleMap?.clear()
                        mGoogleMap?.addMarker(pickUpMarker)
                    }
                }
                dropOffBool -> {
                    dropOffMarker.position(latLng)
                    mGoogleMap?.addMarker(dropOffMarker)

                    if (firstTimeCheckPickUp){
                        mGoogleMap?.clear()
                        mGoogleMap?.addMarker(pickUpMarker)
                        mGoogleMap?.addMarker(dropOffMarker)
                    }else{
                        mGoogleMap?.clear()
                        mGoogleMap?.addMarker(dropOffMarker)
                    }
                }
                else -> Log.e(TAG,"else")
            }
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.btn_pick_up ->{
                pickUpBool = true
                dropOffBool = false
                firstTimeCheckPickUp = true
            }

            R.id.btn_drop_off ->{
                firstTimeCheckPickUp = true
                pickUpBool = false
                dropOffBool = true
            }

            R.id.btn_calc_distance ->{
               // calculateDirections(pickUpMarker,dropOffMarker)
                 FetchURL(this@DriverActivity).execute(getUrl(LatLng(pickUpMarker.position.latitude,pickUpMarker.position.longitude), LatLng(dropOffMarker.position.latitude,pickUpMarker.position.longitude), "distance"), "distance")
            }
        }
    }

    private fun getUrl( origin:LatLng,  dest:LatLng, directionMode:String):String {
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyAyVirgyaPoZXHrNq1NSd1FXoxsgnl5_Hc"
    }

    override fun onTaskDone(vararg values: Any?) {
        if (currentPolyline != null)
            currentPolyline!!.remove()
        currentPolyline = mGoogleMap?.addPolyline(values[0] as PolylineOptions)
    }

    public override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    public override fun onDestroy() {
        mMapView?.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView?.onLowMemory()
    }
}
