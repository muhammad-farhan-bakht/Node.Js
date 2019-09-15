package com.farhan.rideshare.auth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.farhan.rideshare.R
import com.farhan.rideshare.utils.Constant
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.ConnectionResult
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.location.LocationManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.farhan.rideshare.driver.DriverActivity
import com.farhan.rideshare.user.UserActivity
import com.farhan.rideshare.utils.Constant.ERROR_DIALOG_REQUEST
import com.farhan.rideshare.utils.Constant.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.farhan.rideshare.utils.Constant.PERMISSIONS_REQUEST_ENABLE_GPS

class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private var parentView: View? = null
    private var mLocationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        parentView = findViewById(android.R.id.content)

        sign_up_for_account.setOnClickListener { Snackbar.make(parentView!!, "Sign up for an account", Snackbar.LENGTH_SHORT).show() }

        fab.setOnClickListener { searchAction() }
    }

    override fun onResume() {
        super.onResume()
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                Snackbar.make(parentView!!, "Maps permission is granted", Snackbar.LENGTH_SHORT).show()
            }
            else{
                getLocationPermission()
            }
        }
    }


    private fun searchAction() {
        progress_bar.visibility = View.VISIBLE
        fab.alpha = 0f

        if (et_user_name.text.toString().isEmpty()) {
            et_user_name.error = "This field is required"
            return
        }

        if (et_user_pass.text.toString().isEmpty()) {
            et_user_pass.error = "This field is required"
            return
        }

        Handler().postDelayed({
            progress_bar.visibility = View.GONE
            fab.alpha = 1f

            if (et_user_name.text.toString() == Constant.DRIVER_USER_NAME && et_user_name.text.toString() == Constant.DRIVER_USER_PASS) {
                Snackbar.make(parentView!!, "Sign in successfully", Snackbar.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity,DriverActivity::class.java))
                this.finish()
            }
            else if(et_user_name.text.toString() == Constant.USER_USER_NAME && et_user_name.text.toString() == Constant.USER_USER_PASS){
                Snackbar.make(parentView!!, "Sign in successfully", Snackbar.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity,UserActivity::class.java))
                this.finish()
            }else{
                Snackbar.make(parentView!!, "User not found", Snackbar.LENGTH_SHORT).show()
            }
        }, 1000)
    }

    private fun checkMapServices(): Boolean {
        if (isServicesOK()) {
            if (isMapsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    val enableGpsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS)
                }
        val alert = builder.create()
        alert.show()
    }

    private fun isMapsEnabled(): Boolean {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

   private fun isServicesOK(): Boolean {
        Log.e(TAG, "isServicesOK: checking google services version")

        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@LoginActivity)

       when {
           available == ConnectionResult.SUCCESS -> {
               //everything is fine and the user can make map requests
               Log.d(TAG, "isServicesOK: Google Play Services is working")
               return true
           }
           GoogleApiAvailability.getInstance().isUserResolvableError(available) -> {
               //an error occured but we can resolve it
               Log.d(TAG, "isServicesOK: an error occured but we can fix it")
               val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@LoginActivity, available, ERROR_DIALOG_REQUEST)
               dialog.show()
           }
           else -> Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
       }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: called.")
        when (requestCode) {
            PERMISSIONS_REQUEST_ENABLE_GPS -> {
                if (mLocationPermissionGranted) {
                    Snackbar.make(parentView!!, "Maps permission is granted", Snackbar.LENGTH_SHORT).show()
                } else {
                    getLocationPermission()
                }
            }
        }
    }

}
