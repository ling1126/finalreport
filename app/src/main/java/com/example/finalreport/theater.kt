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
import org.json.JSONObject


class theater : AppCompatActivity() {
    private lateinit var myBind:ActivityTheaterBinding
    private val targetUrl =
        "https://tcgbusfs.blob.core.windows.net/dotapp/news.json"
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
                        try { //分析 JSON
                            var news = JSONObject(getString).getJSONArray("News")
//                          for (i in 0..news.length())
                            var jO = news.getJSONObject(1)
                            var  jmessage = jO.getString("chmessage")
//                          var jmessage = jO.getString("url")
                            var jstart = jO.getString("starttime")
                            getString = "start time :$jstart\n" +
                                    "Message : $jmessage"
                        } catch (e: Exception) {
                            Log.d("myTag", "Error : ${e.toString()}")
                        }
                    }
                }
                runOnUiThread {
                    myBind.textView.text = getString
                }
            }
        }
    }
}