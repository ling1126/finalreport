package com.example.finalreport

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.text.method.ScrollingMovementMethod
import java.io.IOException

class theater : AppCompatActivity() {
    private lateinit var myBind:ActivityTheaterBinding
    private val targetUrl = "https://data.ntpc.gov.tw/api/datasets/61C99F42-8A90-4ADC-9C40-BA9E0EA097AA/json?page=0&size=1000"
    private var getString = ""
    private var getString2 = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityTheaterBinding.inflate(layoutInflater)
        setContentView(myBind.root)
//        setContentView(R.layout.activity_theater)
        myBind.textView.movementMethod = ScrollingMovementMethod.getInstance() // 滾動文字

        fetchDataAndDisplayResult()

        myBind.btnUpdate.setOnClickListener {
            Intent(this, NOWsPACE::class.java).apply {
                startActivity(this)
            }
        }
    }
    private fun fetchDataAndDisplayResult() {
        //設定 OKhttp (開始 Download 資料
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder().url(targetUrl).build()

        //work Thread (Coroutines) , 資料分析
        GlobalScope.launch {
            // runBlocking
            try {
                val response = client.newCall(request).execute()
                response.body?.use {
                    getString = it.string()   // 已取得 JSON
                    Log.d("myTag", "$getString")

                    val jsonData = JSONArray(getString)
                    val resultStringBuilder = StringBuilder()

                    for (i in 0 until jsonData.length()) {
                        val jO = jsonData.getJSONObject(i)
                        val jname = jO.getString("name")
                        val jaddress = jO.getString("address")
                        val jphone = jO.getString("tel")

                        // 地址轉經緯
                        val address = jaddress
                        val latLng = getAddressLatLng(this@theater, address)

                        if (latLng != null) {
                            val (latitude, longitude) = latLng
                            withContext(Dispatchers.Main) {
                                Log.d("myTag", "Address: $address, Latitude: $latitude, Longitude: $longitude")

                                getString2 = "Latitude: $latitude \n Longitude: $longitude \n"
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Log.d("myTag", "Unable to convert address to LatLng.")
                            }
                        }

                        val theaterInfo = "電影院名稱 : $jname\n" +
                                "地址 : $jaddress\n" +
                                "電話 :$jphone\n" +
                                "經緯度: \n $getString2"

                        resultStringBuilder.append(theaterInfo)
                        //
                        withContext(Dispatchers.Main) {
                            myBind.textView.text = resultStringBuilder.toString()
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