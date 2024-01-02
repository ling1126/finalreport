package com.example.finalreport

import com.example.finalreport.databinding.ActivityPageIntentBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.io.IOException
import android.util.Log



class pageIntent : AppCompatActivity() {
    private lateinit var myBind: ActivityPageIntentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityPageIntentBinding.inflate(layoutInflater)
        setContentView(myBind.root)

        // 使用範例
        val address = "台北市信義區市府路1號"
        val latLng = getAddressLatLng(this, address)

        if (latLng != null) {
            val (latitude, longitude) = latLng
            myBind.textView2.text = "Latitude: $latitude, Longitude: $longitude"
            Log.d("myTag", "Address: $address, Latitude: $latitude, Longitude: $longitude")
        } else {
            myBind.textView2.text = "Unable to convert address to LatLng."
            Log.d("myTag", "Unable to convert address to LatLng.")
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