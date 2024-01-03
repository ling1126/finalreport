package com.example.finalreport

import com.example.finalreport.databinding.ActivityNowSpaceBinding
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.core.app.ActivityCompat

import android.content.Context
import android.util.Log
import com.example.finalreport.databinding.ActivityTheaterBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.location.Address
import android.location.Geocoder
import java.io.IOException

import java.text.DecimalFormat

class nowSpace : AppCompatActivity() , LocationListener {
    private lateinit var myBind: ActivityNowSpaceBinding
    private val targetUrl = "https://data.ntpc.gov.tw/api/datasets/61C99F42-8A90-4ADC-9C40-BA9E0EA097AA/json?page=0&size=1000"
    private var getString = ""
    private var getString2 = ""
    //tv: TextView  //尋找GPS與網路設定
    // tv1: TextView //與電影院距離

    //建立List，屬性為Poi物件
    private val pois = mutableListOf<Poi>()
    //LocationManager設定
    private lateinit var locationManager: LocationManager

    private var hasGPS = false
    private var hasNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityNowSpaceBinding.inflate(layoutInflater)
        setContentView(myBind.root)

        // 滾動文字
        myBind.tv1.movementMethod = ScrollingMovementMethod.getInstance()

        fetchDataAndDisplayResult()
        list()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var s = ""
        if (hasGPS) s += "设备提供GPS，"
        if (hasNetwork) s += "设备提供网络定位\n"
        myBind.tv.text = s

        if (hasGPS || hasNetwork) {
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
            myBind.tv1.text = "没有提供定位服务"
        }
    }

    class Poi(val name: String, var latitude: Double, var longitude: Double) {
        var distance = 0.0

        // 将信息带入类别属性
        init {
            this.latitude = latitude
        }
    }

    private fun list() {

        try {
            val jsonData = JSONArray(getString)
            println("Parsing JSON Data:")
            for (i in 0 until jsonData.length()) {
                val jO = jsonData.getJSONObject(i)
                val jname = jO.getString("name")
                val jaddress = jO.getString("address")
                val jphone = jO.getString("tel")
                println("jname: $jname, jaddress: $jaddress, jphone: $jphone")



            val address = jaddress
                val latLng = getAddressLatLng(this@nowSpace, address)

                if (latLng != null) {
                    val (latitude, longitude) = latLng
                    println("Latitude: $latitude, Longitude: $longitude")
                    pois.add(Poi(jname, latitude, longitude))
                }
            }
        } catch (e: JSONException) {
            Log.e("myTag", "Error parsing JSON: ${e.toString()}")
        }
    }


    override fun onLocationChanged(location: Location) {
        val logStringBuilder = StringBuilder()
        val coordinatesLog = "目前座標\n經度:${location.longitude}\n緯度:${location.latitude}\n以下為與電影院的距離"
        logStringBuilder.append(coordinatesLog).append("\n")

        for (p1: Poi in pois) {
            p1.distance = distance(location.latitude, location.longitude, p1.latitude, p1.longitude)
        }

        distanceSort(pois)

        for (i in pois.indices) {
            val poiInfo = "名稱:${pois[i].name} \n距離為:${distanceText(pois[i].distance)}\n"
            logStringBuilder.append(poiInfo)
        }

        runOnUiThread {
            myBind.tv1.text = logStringBuilder.toString()
        }
    }

    companion object {
        fun distanceText(distance: Double): String {
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

    private fun distanceSort(poi: MutableList<Poi>) {
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

    private fun fetchDataAndDisplayResult() {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(targetUrl).build()

        GlobalScope.launch {
            try {
                val response = client.newCall(request).execute()
                response.body?.use {
                    getString = it.string()
                    Log.d("myTag", "$getString")

                    val jsonData = JSONArray(getString)
                    val resultStringBuilder = StringBuilder()

                    for (i in 0 until jsonData.length()) {
                        val jO = jsonData.getJSONObject(i)
                        val jname = jO.getString("name")
                        val jaddress = jO.getString("address")
                        val jphone = jO.getString("tel")

                        val address = jaddress
                        val latLng = getAddressLatLng(this@nowSpace, address)

                        if (latLng != null) {
                            val (latitude, longitude) = latLng
                            withContext(Dispatchers.Main) {
                                Log.d("myTag", "Address: $address, Latitude: $latitude, Longitude: $longitude")
                                getString2 = "Latitude: $latitude \n, Longitude: $longitude"
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Log.d("myTag", "Unable to convert address to LatLng.")
                            }
                        }

                        val theaterInfo = "电影院 : $jname\n" +
                                "Telephone :$jphone\n" +
                                "address : $jaddress\n" +
                                "經緯度:\n $getString2"

                        resultStringBuilder.append(theaterInfo)
                        //
                        withContext(Dispatchers.Main) {
                            Log.d("myTag", "$theaterInfo ")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("myTag", "Error: ${e.toString()}")
            } catch (e: JSONException) {
                Log.e("myTag", "Error: ${e.toString()}")
            }
        }
    }

    private fun getAddressLatLng(context: Context, addressString: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context)
        try {
            val addresses: List<Address> = geocoder.getFromLocationName(addressString, 1) as List<Address>
            if (addresses.isNotEmpty()) {
                val latitude = addresses[0].latitude
                val longitude = addresses[0].longitude
                return Pair(latitude, longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}