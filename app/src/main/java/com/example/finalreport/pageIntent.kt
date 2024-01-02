package com.example.finalreport

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class pageIntent: AppCompatActivity() {


    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page_intent    )

        barChart = findViewById(R.id.chart)

        fetchDataAndCreateBarChart()
    }

    private fun fetchDataAndCreateBarChart() {
        val url = "https://boxoffice.tfi.org.tw/api/export?start=2023/12/18&end=2023/12/24"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()

                val response = client.newCall(request).execute()
                val jsonData = JSONObject(response.body?.string())

                val entries = mutableListOf<BarEntry>()
                val xAxisLabels = mutableListOf<String>()

                val listArray = jsonData.getJSONArray("list")

                for (i in 0 until listArray.length()) {
                    val jO = listArray.getJSONObject(i)
                    val jname = jO.getString("name")
                    val jtotalTickets = jO.getInt("totalTickets").toFloat() // Assuming totalTickets is an integer

                    entries.add(BarEntry(i.toFloat(), jtotalTickets))
                    xAxisLabels.add(jname)
                }

                val barDataSet = BarDataSet(entries, "電影熱度")
                barDataSet.valueTextColor = Color.BLACK
                barDataSet.valueTextSize = 10f
                barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()

                val barData = BarData(barDataSet)

                runOnUiThread {
                    setupBarChart(barData, xAxisLabels)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                Log.e("FetchDataError", "Error fetching data: ${e.message}")
            } catch (ex: Exception) {
                ex.printStackTrace()
                Log.e("FetchDataError", "Exception: ${ex.message}")
            }
        }
    }

    private fun setupBarChart(barData: BarData, xAxisLabels: List<String>) {
        barChart.data = barData
        barChart.setFitBars(true)
        barChart.animateY(1500)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.textSize = 12f
        xAxis.labelRotationAngle = -45f // 旋转角度


        barChart.setVisibleXRangeMaximum(3f)
        barChart.isDragEnabled = true
        barChart.invalidate()
    }
}

