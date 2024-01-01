package com.example.finalreport

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalreport.databinding.ActivityMainBinding
import com.example.finalreport.databinding.ActivityTheaterBinding

class MainActivity : AppCompatActivity() {
    private lateinit var myBind: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myBind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(myBind.root)

        myBind.button.setOnClickListener {
            Intent(this@MainActivity,theater::class.java).apply {
            startActivity(this)
            }
        }
    }
}