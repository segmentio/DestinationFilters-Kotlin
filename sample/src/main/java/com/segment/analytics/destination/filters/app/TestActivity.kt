package com.segment.analytics.destination.filters.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.segment.analytics.kotlin.core.utilities.toBaseEvent
import com.segment.analytics.plugins.DestinationFilters
import com.segment.analytics.substrata.kotlin.JSObject
import com.segment.analytics.substrata.kotlin.JsonElementConverter
import kotlinx.serialization.json.jsonObject

class TestActivity : AppCompatActivity() {
    val analytics = MainApplication.analytics

    private fun Boolean.emoji(): String {
        return if (this) {
            "✅"
        } else {
            "❌"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val textView = findViewById<TextView>(R.id.text_view)

        val testProgress = StringBuilder()

        val analytics = MainApplication.analytics

        // Check engine init
        val plugin = analytics.find(DestinationFilters::class)
        val engine = plugin?.engine
        testProgress.append("Engine not null: ${(engine != null).emoji()}\n")

        if (engine == null) {
            return
        }

        // Test if tsub is loaded
        val tsubLoaded = engine.await {
            return@await evaluate("""(typeof dest_filters.evaluateDestinationFilters === "function")""")
        }

        testProgress.append("tsub loaded correctly: ${(tsubLoaded == true).emoji()}\n")

        // Test if createDestinationFilter exists and works
        val result = engine.await(global = true) {
            evaluate(
                """
            dest_filters.evaluateDestinationFilters(
               {
                 "matchers": [
                   {
                     "ir": "",
                     "type": "all"
                   }
                 ],
                 "scope": "destinations",
                 "target_type": "workspace::project::destination::config",
                 "transformers": [
                   [
                     {
                       "type": "drop_properties",
                       "config": {
                         "drop": {
                           "context.device": [
                             "id",
                             "advertisingId"
                           ]
                         }
                       }
                     }
                   ]
                 ],
                 "destinationName": "doesnt matter"
               },
               {
                 "anonymousId": "fdae402a-dd8a-4117-9a9b-3aca3a15c36c",
                 "context": {
                   "app": {
                     "name": "destination-filters-kotlin",
                     "namespace": "com.segment.analytics.destination.filters.app",
                     "version": "1.0"
                   },
                   "device": {
                     "id": "bef8e59bc1db1cc7b80afa4c5bfad93c770bd01a7b10d770fc1dce7fe9864856",
                     "manufacturer": "Google",
                     "model": "Android SDK built for x86",
                     "name": "generic_x86",
                     "type": "android"
                   }
                 },
                 "integrations": {},
                 "event": "Application Opened",
                 "messageId": "7ce02e33-a2c4-4b47-bced-8dda01f7b5b2",
                 "timestamp": "2022-08-22T20:33:34.908Z",
                 "properties": {
                   "version": "1.0"
                 },
                 "type": "track",
                 "writeKey": "93EMLzmXzP6EJ3cJOhdaAgEVNnZjwRqA"
               }
            )
            """.trimIndent()
            )
        }
        testProgress.append("tsub is functioning: \n")
        testProgress.append("\tResult is a json object: ${(result is JSObject).emoji()}\n")
        engine.sync {
            JsonElementConverter.read(result).jsonObject.toBaseEvent().let {
                testProgress.append("\tEvent is non null: ${(it != null).emoji()}\n")
                val t = it!!.context["device"]?.jsonObject?.contains("id")?.not() ?: false
                testProgress.append("\tEvent context.device.id is filtered: ${t.emoji()}\n")
            }
        }



        // Test if update creates filters
        // Run an event through it all
        // FIXME for now being done via writekey... Need to make this into UNIT Tests later

        textView.text = testProgress.toString()
    }
}
