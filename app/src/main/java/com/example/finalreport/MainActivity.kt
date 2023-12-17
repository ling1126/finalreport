package com.example.finalreport

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.text.DecimalFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() , LocationListener {
    private lateinit var tv: TextView  //尋找GPS與網路設定
    private lateinit var tv1: TextView //與電影院距離
    private lateinit var title: TextView //標題
    //建立List，屬性為Poi物件
    private val Pois = ArrayList<Poi>()
    //LocationManager設定
    private lateinit var locationManager: LocationManager
    private  var hasGPS:Boolean=false
    private  var hasNetwork:Boolean=false
    //class MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //定義
        int()
        tv1.setMovementMethod(ScrollingMovementMethod.getInstance())//滾動文字
        //建立物件，並放入List裡 (建立物件需帶入名稱、緯度、經度)
        list()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        //檢視gps network
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        hasNetwork = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
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
               ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1.0F,this)
        }else{
            tv1.text="沒有提供定位服務"
        }
    }
    private fun int(){
        tv=findViewById(R.id.tv)
        tv1=findViewById(R.id.tv1)
        title=findViewById(R.id.title)
    }
    class Poi(val name: String,var latitude: Double,longitude:Double) {
        val longitude: Double //取得店家經度
        var distance = 0.0//取的店家距離
        //建立物件時需帶入店家名稱、店家緯度、店家經度
        init {//將資訊帶入類別屬性
            this.latitude = latitude
            this.longitude = longitude
        }
    }
    private fun list() {
        val pois = mutableListOf<Poi>()
        pois.add(Poi("威秀影城股份有限公司板橋分公司", 25.014105657798165, 121.46715243252677))
        pois.add(Poi("板橋秀泰影城股份有限公司", 25.013209935465266, 121.46208878783273))
        pois.add(Poi("林園電影城戲院", 25.00933780351172, 121.45465836507512))
        pois.add(Poi("鴻金寶麻吉戲院(萬念福開發事業股份有限公司)", 25.023086143226138, 121.42480572488105))
        pois.add(Poi("威秀影城股份有限公司中和環球分公司", 25.007177645471415, 121.47542257229885))
        pois.add(Poi("土城秀泰影城股份有限公司", 25.062455091672728, 121.49721209610084))
        pois.add(Poi("天台戲院股份有限公司", 25.007138753227718, 121.47499341851896))
        pois.add(Poi("國賓影城股份有限公司林口分公司", 25.074213827153127, 121.3684236505742))
        pois.add(Poi("威秀影城股份有限公司新北林口分公司", 25.077934743829545, 121.3811752938347))
        pois.add(Poi("樹林秀泰影城股份有限公司", 24.997416671879666, 121.42864334179383))
        pois.add(Poi("美麗新娛樂股份有限公司淡海分公司", 25.199727004217948, 121.43854731151075))
        pois.add(Poi("喜樂時代影城股份有限公司永和分公司", 25.008830914327948, 121.50777787280424))
        pois.add(Poi("國賓影城股份有限公司淡水分公司", 25.177177195438922, 121.42960578823048))
        pois.add(Poi("美麗新娛樂股份有限公司新莊分公司", 25.061471484342867, 121.45374736576008))
        Pois.clear()
        Pois.addAll(pois)
    }
    override fun onLocationChanged(location: Location) {
        val logStringBuilder = StringBuilder()
        val coordinatesLog = "目前座標-經度:${location.longitude} ,緯度:${location.latitude}"
        logStringBuilder.append(coordinatesLog).append("\n")
        for (p1: Poi in Pois) {
            p1.distance = distance(location.latitude, location.longitude, p1.latitude, p1.longitude)
        }
        DistanceSort(Pois)
        for (i in 0 until Pois.size) {
            val poiInfo = "名稱:${Pois[i].name} ,距離為:${DistanceText(Pois[i].distance)}\n"
            logStringBuilder.append(poiInfo)
        }
        runOnUiThread {
            tv1.text = logStringBuilder.toString()
        }
    }
    private fun DistanceText(distance: Double): String {//將距離轉換為文字描述
        //如果距離小於 1000 ，就以「m」為單位，否則以「km」為單位。
        return if (distance < 1000) distance.toInt().toString() + "m" else DecimalFormat("#.00").format(
            distance / 1000
        ) + "km"
    }
    private fun DistanceSort(poi: ArrayList<Poi>) {//列表按照 distance 屬性進行升序排序
        poi.sortBy { it.distance }
    }
    //distanc計算兩組經緯度座標之間的球面距離
    fun distance(longitude1: Double, latitude1: Double, longitude2: Double, latitude2: Double): Double {
        //經緯度轉換成弧度
        val radLatitude1 = latitude1 * Math.PI / 180
        val radLatitude2 = latitude2 * Math.PI / 180
        //計算兩點的差異
        val l = radLatitude1 - radLatitude2
        val p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180
        //使用 Haversine 公式計算球面距離
        var distance = 2 * Math.asin(
            Math.sqrt(
                Math.pow(Math.sin(l / 2), 2.0)
                        + (Math.cos(radLatitude1) * Math.cos(radLatitude2)
                        * Math.pow(Math.sin(p / 2), 2.0))
            )
        )
        distance = distance * 6378137.0//將弧長轉換為實際距離乘以地球的半徑
        distance = (Math.round(distance * 10000) / 10000).toDouble()//四捨五入
        return distance
    }
    //生命週期開始
    override fun onResume() {
        super.onResume()
    }
    //生命週期結束
    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }
    //應用程式請求使用者權限並獲得使用者對權限請求的回應時被調用
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}