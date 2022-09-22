package com.segment.analytics.destination.filters.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MainActivity : AppCompatActivity() {
    val analytics = MainApplication.analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.btn_checkout).setOnClickListener {
            analytics.track("User Checkout")
        }

        findViewById<TextView>(R.id.btn_exit).setOnClickListener {
            analytics.track("Exit Clicked")
        }

        findViewById<TextView>(R.id.btn_purchase).setOnClickListener {
            analytics.track("Event Name", buildJsonObject {
                put("string", "string")
//                put("date", Json.encodeToJsonElement(Date(1391038728131)))
                put("bool", true)
                put("number", 42)
                put("zero", 0)
                put("zero", JsonNull)
            })
        }

        findViewById<TextView>(R.id.btn_register).setOnClickListener {
            analytics.track("User Registered")
        }
    }
}
