package com.example.honeyiamhome

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var trackingPermission = 0;
    private val MY_CODE = 999;
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // SharedPref data recovered
        val sp : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val HomeLocationLongitude = sp.getString("HomeLocationLongitude", null);
        val HomeLocationLatitude = sp.getString("HomeLocationLatitude", null);

        ClearHomeLocation.visibility = View.INVISIBLE;
        SetAsHome.visibility = View.INVISIBLE

        val clearHomeLocationButton = findViewById<Button>(R.id.ClearHomeLocation)
        clearHomeLocationButton.setOnClickListener {
            Log.d(TAG, "ClearHomeLocation")
            //findViewById<TextView>(R.id.CurrentLocationLatitude).text = "";
            //findViewById<TextView>(R.id.CurrentLocationLongitude).text = "";
            //findViewById<TextView>(R.id.Accuracy).text = "";
            findViewById<TextView>(R.id.HomeLocationLatitude).text = "";
            findViewById<TextView>(R.id.HomeLocationLongitude).text = "";

            // init sp
            val editor = sp.edit();
            editor.putString("HomeLocationLongitude", null);
            editor.putString("HomeLocationLatitude", null);
            editor.apply();
            setHomeText()
            clearHomeLocationButton.visibility = View.INVISIBLE;
        }  // clearHomeLocationButton


        val startTrackingButton = findViewById<Button>(R.id.StartTracking)
        startTrackingButton.setOnClickListener {
            Log.d(TAG, "start tracking location")
            // Mean that we don't have permission
            if (trackingPermission != 1) {
                val isGetPermission : Boolean
                isGetPermission = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!isGetPermission) {
                    Log.d(TAG, "User hasn't give his permission")
                    ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),MY_CODE)
                } else {
                    Log.d(TAG, "User give his permission before")
                    getLocation()
                }
            }   //  If user hasn't give his permission
            else {
                // start working on location. the app will clean all fields and let the user know about that
                startTrackingButton.text = "Stop tracking"
                // Clean all tabs
                findViewById<TextView>(R.id.CurrentLocationLatitude).text = "";
                findViewById<TextView>(R.id.CurrentLocationLongitude).text = "";
                findViewById<TextView>(R.id.Accuracy).text = "";
                findViewById<TextView>(R.id.IsHome).text = "";
                trackingPermission = 0;
            }  // if user has give his permission
            setHomeText()
        }  // startTrackingButton
        val setAsHomeLocationButton = findViewById<Button>(R.id.SetAsHome)
        //setAsHomeLocationButton.visibility = View.INVISIBLE
        setAsHomeLocationButton.setOnClickListener {
            Log.d(TAG, "SetAsHome")

            // Set the new home location on xml
            findViewById<TextView>(R.id.HomeLocationLongitude).text = findViewById<TextView>(R.id.CurrentLocationLongitude).text.toString();
            findViewById<TextView>(R.id.HomeLocationLatitude).text = findViewById<TextView>(R.id.CurrentLocationLatitude).text.toString();

            // update sp
            val editor = sp.edit();
            editor.putString("HomeLocationLongitude",findViewById<TextView>(R.id.CurrentLocationLongitude).text.toString());
            editor.putString("HomeLocationLatitude",findViewById<TextView>(R.id.CurrentLocationLatitude).text.toString());
            editor.apply();
            setHomeText()
            clearHomeLocationButton.visibility = View.VISIBLE;
        }  // setAsHomeLocationButton
    }  // OnCreate

    // Some Functions

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == MY_CODE)
        {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
               getLocation()
            }
            else{
                val locationPop = AlertDialog.Builder(this);
                locationPop.setTitle("App need to use your location");
                locationPop.show();

            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun getLocation() {
        // this function will bring the location
        Log.d(TAG, "Find location")
        val trackButton = findViewById<Button>(R.id.StartTracking)
        trackingPermission = 1

        mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
            //try {} // try
            val location: Location? = task.result
            if(location == null){ Log.d(TAG, "some problems with location")}
            else
            {
                // if we got a good location we will check is resolution
                Log.d(TAG, "we got a good location we will check is resolution")
                if(location.accuracy <= 50){
                    SetAsHome.visibility = View.VISIBLE
                    Log.d(TAG, "Mobile got 50m resolution")
                }
                findViewById<TextView>(R.id.CurrentLocationLongitude).text = "Longitude " + location.longitude.toString() + 0x00B0.toChar() + "  [Deg]";
                findViewById<TextView>(R.id.CurrentLocationLatitude).text = "Latitude " + location.latitude.toString() + 0x00B0.toChar() + " [Deg]";
                findViewById<TextView>(R.id.Accuracy).text = "Accuracy " + location.accuracy.toString() + " [Meter]"
            }
        }
    } // getLocation

    @SuppressLint("SetTextI18n")
    fun setHomeText(){
        if(findViewById<TextView>(R.id.HomeLocationLongitude).text != "" && findViewById<TextView>(R.id.HomeLocationLatitude).text != ""){
            // when showing the main screen, add a check - if the user's home location is already stored
            findViewById<TextView>(R.id.HomeLocationText).text = "Your home location is defined as ";
        }
        else{findViewById<TextView>(R.id.HomeLocationText).text = "Your home location isn't defined";}
    }
} // MainActivity


