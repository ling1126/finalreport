package com.example.finalreport

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalreport.databinding.ActivityMainBinding
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.finalreport.databinding.ActivityTheaterBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var myBind:ActivityMainBinding

    private lateinit var locationManager: LocationManager

    private var hasGPS = false
    private var hasNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(myBind.root)


        //theater
        myBind.btnTheater.setOnClickListener {
            Intent(this, theater::class.java).apply {
            startActivity(this)
            }
        }
        //now
        myBind.btnNow.setOnClickListener {
            Intent(this, chart2::class.java).apply {
                startActivity(this)
            }
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //檢視gps network
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        //檢查是否提供定位
        var s = ""
        if (hasGPS) s += "設備提供GPS，"
        if (hasNetwork) s += "設備提供網路定位\n"
        Log.d("myTag", "$s")
        //tv.text = s

        //登記location/GPS
        if (hasGPS || hasNetwork) {
            //檢查權限是否開啟locationManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //取得使用者同意
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                //更新本人位置
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0F, this)
            }
        } else {
            //tv1.text = "沒有提供定位服務"
            Log.d("myTag", "沒有提供定位服務")
        }

    }

    override fun onLocationChanged(p0: Location) {
     //   TODO("Not yet implemented")
    }
}
