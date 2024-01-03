package com.example.finalreport

import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class chart3 : AppCompatActivity() {
    private lateinit var chart: BarChart
    private lateinit var spinner: Spinner // 假設有一個 Spinner 用來選擇國家
    private lateinit var getString: String
    private lateinit var button: Button

    private lateinit var allData: ArrayList<oneItem>

    private fun filterDataByCounty(selectedCounty: String): ArrayList<oneItem> {
        val filteredData = ArrayList<oneItem>()

        for (item in allData) {
            if (item.icounty== selectedCounty) {
                filteredData.add(item)
            }
        }

        return filteredData
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart3)

        chart = findViewById(R.id.chart33)
        spinner = findViewById(R.id.spinner2)
        button = findViewById(R.id.btn3)
        allData = ArrayList<oneItem>() //建立起使值
        val countyList = ArrayList<String>()

        val uniqueCounties = HashSet<String>()



        button.setOnClickListener {
            val url = "https://boxoffice.tfi.org.tw/api/export?start=2023/12/18&end=2023/12/24"
            var client = OkHttpClient.Builder().build()
            val request = Request.Builder().url(url).build()

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val response = client.newCall(request).execute()

                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("myTag", jsonString)

                        val allRecord = JSONObject(jsonString)
                        val listArray = allRecord.getJSONArray("list")

                        // 清空資料
                        allData.clear()
                        countyList.clear()
                        uniqueCounties.clear()

                        for (i in 0 until listArray.length()) {
                            val item = listArray.getJSONObject(i)
                            val jcounty = item.getString("country")
                            val jname = item.getString("name")
                            val jcount = item.getInt("totalTickets").toFloat()

                            if (!uniqueCounties.contains(jcounty)) {
                                uniqueCounties.add(jcounty)
                                countyList.add(jcounty)
                            }

                            allData.add(oneItem(jcounty, jname, jcount))
                        }

                        runOnUiThread {
                            val spAdapter = ArrayAdapter(
                                this@chart3,
                                android.R.layout.simple_spinner_dropdown_item,
                                countyList
                            )
                            spinner.adapter = spAdapter
                            spAdapter.notifyDataSetChanged()

                            // 處理 Spinner 的選擇事件
                            spinner.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>?,
                                        view: View?,
                                        position: Int,
                                        id: Long
                                    ) {
                                        val selectedCounty = countyList[position]
                                        val filteredData = filterDataByCounty(selectedCounty)

                                        // 更新柱狀圖
                                        updateBarChart(filteredData)
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                        // Do nothing
                                    }
                                }
                        }
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
    }


    private fun updateBarChart(filteredData: ArrayList<oneItem>) {
        val sortedData = filteredData.sortedByDescending { it.icount }
        val entries = ArrayList<BarEntry>()
        val xAxisLabels = ArrayList<String>()

        sortedData.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.icount))
            xAxisLabels.add(item.iname)
        }

        val barDataSet = BarDataSet(entries, "Tickets")
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS.toList())

        val data = BarData(barDataSet)
        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
        chart.xAxis.labelRotationAngle = 45f
        chart.xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.granularity = 1f
        chart.xAxis.textSize = 12f

        chart.setVisibleXRangeMaximum(3f)
        chart.isDragEnabled = true

        chart.invalidate()
    }


    }








