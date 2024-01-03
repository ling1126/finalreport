package com.example.finalreport

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
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


class chart2 : AppCompatActivity() {
    private lateinit var barChart: BarChart
    private lateinit var progressBar: ProgressBar
    private lateinit var btn:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart2)
        barChart = findViewById(R.id.chart22)
        progressBar = findViewById(R.id.progressBar1)
        btn = findViewById(R.id.btn2)

        btn.setOnClickListener {
            Intent(this, chart3::class.java).apply {
                startActivity(this)
            }
        }


        fetchDataAndCreateBarChart()
    }



    private fun fetchDataAndCreateBarChart() {
        barChart.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
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

                // Create a list of pairs with entry value and its index
                val entryList = mutableListOf<Pair<Float, Int>>()

                for (i in 0 until listArray.length()) {
                    val jO = listArray.getJSONObject(i)
                    val jname = jO.getString("name")
                    val jtotalTickets = jO.getInt("totalTickets").toFloat()

                    entryList.add(Pair(jtotalTickets, i))
                    xAxisLabels.add(jname)
                }

                // Sort the entries based on their values
                entryList.sortByDescending { it.first }

                // Reconstruct entries and xAxisLabels based on sorted order
                entryList.forEachIndexed { index, pair ->
                    entries.add(BarEntry(index.toFloat(), pair.first))
                    xAxisLabels.add(listArray.getJSONObject(pair.second).getString("name"))
                }

                val barDataSet = BarDataSet(entries, "電影熱度")
                barDataSet.valueTextColor = Color.BLACK
                barDataSet.valueTextSize = 10f
                barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()

                val barData = BarData(barDataSet)

                runOnUiThread {
                    barChart.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    setupBarChart(entries, xAxisLabels, barData)

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




    private fun setupBarChart(entries: List<BarEntry>, xAxisLabels: List<String>, barData: BarData) {



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
        xAxis.labelRotationAngle = -45f

        //barChart.setNoDataText("loading")
        //barChart.setNoDataTextColor(Color.BLACK) // 設置文字顏色（可選）




        barChart.setVisibleXRangeMaximum(3f)
        barChart.isDragEnabled = true

        barChart.invalidate()




    }
}