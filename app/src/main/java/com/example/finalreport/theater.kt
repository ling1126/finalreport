package com.example.finalreport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.example.finalreport.databinding.ActivityTheaterBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class theater : AppCompatActivity() {
    private lateinit var myBind:ActivityTheaterBinding
    private val targetUrl = "https://data.ntpc.gov.tw/api/datasets/61C99F42-8A90-4ADC-9C40-BA9E0EA097AA/json?page=0&size=1000"
    //private val targetUrl = "https://boxoffice.tfi.org.tw/api/export?start=2023/12/18&end=2023/12/24"
    private var getString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityTheaterBinding.inflate(layoutInflater)
        setContentView(myBind.root)
//        setContentView(R.layout.activity_theater)

        myBind.btnUpdate.setOnClickListener {
            //設定 OKhttp (開始 Download 資料
            val client = OkHttpClient.Builder().build()
            val request = Request.Builder().url(targetUrl).build()

            //work Thread (Coroutines) , 資料分析
            GlobalScope.launch {
                // runBlocking
                val getData = runBlocking {
                    var response = client.newCall(request).execute() //downloading
                    //request.body.run {  //data
                    response.body.run {
                        getString = string()   //已取得 JSON
                        Log.d("myTag", "$getString")
                        try {
                            val jsonData = JSONArray(getString)
                            if (jsonData.length() > 1) {
                                val jO = jsonData.getJSONObject(1)
                                val jname = jO.getString("name")
                                val jaddress = jO.getString("address")
                                val jphone = jO.getString("tel")
                                getString = "電影院 : $jname\n" +
                                        "Telephone :$jphone\n" +
                                        "address : $jaddress"
                            } else {
                                Log.d("myTag", "Error: JSON array ")
                            }
                        } catch (e: JSONException) {
                            Log.d("myTag", "Error: ${e.toString()}")
                        }

                    }
                }
                runOnUiThread {
                    myBind.textView.text = getString
                }
                //save to file. 資料存檔
            }
        }

    }
}