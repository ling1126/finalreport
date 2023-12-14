package com.example.finalreport

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() , LocationListener {
    //LocationManager設定
    private lateinit var locationManager: LocationManager
    private  var hasGPS:Boolean=false
    private  var hasNetwork:Boolean=false
    //class MainActivity

    //生命週期開始
    override fun onResume() {
        super.onResume()
    }
    //生命週期結束
    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }
    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }

}