package com.example.finalreport

import com.example.finalreport.databinding.ActivityPageIntentBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import android.util.Log


class pageIntent : AppCompatActivity() {
    private lateinit var myBind: ActivityPageIntentBinding
    private val targetUrl = "https://data.ntpc.gov.tw/api/datasets/61C99F42-8A90-4ADC-9C40-BA9E0EA097AA/json?page=0&size=1000"
    private var getString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityPageIntentBinding.inflate(layoutInflater)
        setContentView(myBind.root)


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

                            val theaterInfo = "电影院 : $jname\n" +
                                    "Telephone :$jphone\n" +
                                    "address : $jaddress\n\n"

                            resultStringBuilder.append(theaterInfo)

                            // 如果需要将每个地址转换为经纬度，可以在这里添加相应的逻辑
                        }

                        withContext(Dispatchers.Main) {
                            myBind.textView2.text = resultStringBuilder.toString()
                            Log.d("myTag", "Output all data")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("myTag", "Error: ${e.toString()}")
                } catch (e: JSONException) {
                    Log.e("myTag", "Error: ${e.toString()}")
                }
            }
    }
}