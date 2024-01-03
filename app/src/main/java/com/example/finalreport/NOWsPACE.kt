package com.example.finalreport

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.example.finalreport.databinding.ActivityNowsPaceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.text.DecimalFormat

class NOWsPACE : AppCompatActivity() , LocationListener {
    private lateinit var myBind:ActivityNowsPaceBinding

    private val targetUrl = "https://data.ntpc.gov.tw/api/datasets/61C99F42-8A90-4ADC-9C40-BA9E0EA097AA/json?page=0&size=1000"
    private var getString = ""
    private var getString2 = ""
    private var getString3 = ""


    //建立List，屬性為Poi物件
    private val pois = mutableListOf<NOWsPACE.Poi>()
    //LocationManager設定
    private lateinit var locationManager: LocationManager

    private var hasGPS = false
    private var hasNetwork = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityNowsPaceBinding.inflate(layoutInflater)
        setContentView(myBind.root)

        // 滾動文字
        myBind.tv1.movementMethod = ScrollingMovementMethod.getInstance()
        //建立物件，並放入List裡 (建立物件需帶入名稱、緯度、經度)
        list()

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //檢視gps network
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        var s = ""
        if (hasGPS) s += "設備提供GPS，"
        if (hasNetwork) s += "設備提供網路定位\n"
        Log.e("myTag", "$s")
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
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1.0F, this)
            }
        } else {
            //tv1.text = "沒有提供定位服務"
            Log.d("myTag", "沒有提供定位服務")
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
        pois.add(Poi("天台戲院股份有限公司", 25.007138753227718, 121.47499341851896))
        pois.add(Poi("鴻金寶麻吉戲院(萬念福開發事業股份有限公司)", 25.023086143226138, 121.42480572488105))
        pois.add(Poi("威秀影城股份有限公司中和環球分公司", 25.007177645471415, 121.47542257229885))
        pois.add(Poi("威秀影城股份有限公司板橋分公司", 25.014105657798165, 121.46715243252677))
        pois.add(Poi("板橋秀泰影城股份有限公司", 25.013209935465266, 121.46208878783273))
        pois.add(Poi("國賓影城股份有限公司林口分公司", 25.074213827153127, 121.3684236505742))
        pois.add(Poi("國賓影城股份有限公司新莊分公司", 25.063719900000002, 121.45849229999999))
        pois.add(Poi("威秀影城股份有限公司新北林口分公司", 25.0713425, 121.36533089999999))
        pois.add(Poi("樹林秀泰影城股份有限公司", 24.997416671879666, 121.42864334179383))
        pois.add(Poi("土城秀泰影城股份有限公司", 25.062455091672728, 121.49721209610084))
        pois.add(Poi("美麗新娛樂股份有限公司淡海分公司", 25.199727004217948, 121.43854731151075))
        pois.add(Poi("喜樂時代影城股份有限公司永和分公司", 25.008830914327948, 121.50777787280424))
        pois.add(Poi("國賓影城股份有限公司淡水分公司", 25.177177195438922, 121.42960578823048))
        pois.add(Poi("美麗新娛樂股份有限公司新莊分公司", 25.061471484342867, 121.45374736576008))
        pois.add(Poi("林園電影城戲院(暫時歇業中)", 25.0087121, 121.45447980000002 ))
        pois.add(Poi("華麗電影院(暫時歇業中)", 25.0087121, 121.45447980000002))
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


                        val theaterInfo = "電影院名稱 : $jname\n" +
                                "地址 : $jaddress\n" +
                                "電話 :$jphone\n" +
                                "經緯度: \n "

                        getString2= "電影院名稱 : $jname\n"



                        resultStringBuilder.append(theaterInfo)
                        //
                        withContext(Dispatchers.Main) {
                            //myBind.textView.text = resultStringBuilder.toString()
                            Log.w("myTag", "$theaterInfo ")
                        }
                    }
                }
            } catch (e: IOException) {
                Log.w("myTag", "Error: ${e.toString()}")
            } catch (e: JSONException) {
                Log.w("myTag", "Error: ${e.toString()}")
            }

        }
    }

    override fun onLocationChanged(location: Location) {
        val logStringBuilder = StringBuilder()
        //val coordinatesLog = "目前座標\n經度:${location.longitude}\n緯度:${location.latitude}\n以下為與電影院的距離"
        myBind.textView3.text = "目前座標 : \n經度:${location.longitude}\n緯度:${location.latitude}"
        Log.e("myTag", "輸出")
        //logStringBuilder.append(coordinatesLog).append("\n")

        for (p1: Poi in pois) {
            p1.distance = distance(location.latitude, location.longitude, p1.latitude, p1.longitude)
        }

        DistanceSort(pois)

        for (i in pois.indices) {
            val poiInfo = "名稱:${pois[i].name} \n距離為:${DistanceText(pois[i].distance)}\n"
            logStringBuilder.append(poiInfo)
            Log.e("myTag", "放棄...")
        }

        runOnUiThread {
            myBind.tv1.text = logStringBuilder.toString()
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
        locationManager.removeUpdates(this@NOWsPACE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 處理權限回調
    }
}

