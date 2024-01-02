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
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.finalreport.databinding.ActivityTheaterBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(), LocationListener {
    private lateinit var myBind:ActivityMainBinding
    private lateinit var tv: TextView  //尋找GPS與網路設定
    private lateinit var tv1: TextView //與電影院距離
    private lateinit var title: TextView //標題
    //建立List，屬性為Poi物件
    private val pois = mutableListOf<Poi>()
    //LocationManager設定
    private lateinit var locationManager: LocationManager
    private var hasGPS = false
    private var hasNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(myBind.root)

        myBind.button.setOnClickListener {
            Intent(this, theater::class.java).apply {
            startActivity(this)
            }
        }

        //定義
        tv = myBind.tv
        tv1 = myBind.tv1
        title = myBind.title
        tv1.movementMethod = ScrollingMovementMethod.getInstance() // 滾動文字

        //建立物件，並放入List裡 (建立物件需帶入名稱、緯度、經度)
        list()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //檢視gps network
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var s = ""
        if (hasGPS) s += "設備提供GPS，"
        if (hasNetwork) s += "設備提供網路定位\n"
        tv.text = s

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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0F, this)
            }
        } else {
            tv1.text = "沒有提供定位服務"
        }
    }

    class Poi(val name: String, var latitude: Double, longitude: Double) {
        val longitude: Double = longitude // 取得店家經度
        var distance = 0.0 // 取的店家距離

        // 將資訊帶入類別屬性
        init {
            this.latitude = latitude
        }
    }

    private fun list() {
        pois.add(Poi("威秀影城股份有限公司板橋分公司", 25.014105657798165, 121.46715243252677))
        // 加入其他電影院資訊...
    }

    override fun onLocationChanged(location: Location) {
        val logStringBuilder = StringBuilder()
        val coordinatesLog = "目前座標\n經度:${location.longitude}\n緯度:${location.latitude}\n以下為與電影院的距離"
        logStringBuilder.append(coordinatesLog).append("\n")

        for (p1: Poi in pois) {
            p1.distance = distance(location.latitude, location.longitude, p1.latitude, p1.longitude)
        }

        DistanceSort(pois)

        for (i in pois.indices) {
            val poiInfo = "名稱:${pois[i].name} \n距離為:${DistanceText(pois[i].distance)}\n"
            logStringBuilder.append(poiInfo)
        }

        runOnUiThread {
            tv1.text = logStringBuilder.toString()
        }
    }

    companion object {
        fun DistanceText(distance: Double): String {
            return if (distance < 1000) distance.toInt().toString() + "m" else DecimalFormat("#.00").format(
                distance / 1000
            ) + "km"
        }

        fun distance(longitude1: Double, latitude1: Double, longitude2: Double, latitude2: Double): Double {
            val radLatitude1 = latitude1 * Math.PI / 180
            val radLatitude2 = latitude2 * Math.PI / 180
            val l = radLatitude1 - radLatitude2
            val p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180
            var distance = 2 * Math.asin(
                Math.sqrt(
                    Math.pow(Math.sin(l / 2), 2.0)
                            + (Math.cos(radLatitude1) * Math.cos(radLatitude2)
                            * Math.pow(Math.sin(p / 2), 2.0))
                )
            )
            distance = distance * 6378137.0
            distance = (Math.round(distance * 10000) / 10000).toDouble()
            return distance
        }
    }

    private fun DistanceSort(poi: MutableList<Poi>) {
        poi.sortBy { it.distance }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 應該處理權限回調
    }
}
