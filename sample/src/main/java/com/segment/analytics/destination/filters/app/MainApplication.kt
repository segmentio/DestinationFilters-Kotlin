package com.segment.analytics.destination.filters.app

import android.app.Application
import com.segment.analytics.destination.filters.app.filters.WebhookPlugin
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.core.platform.Plugin
import com.segment.analytics.kotlin.core.utilities.updateJsonObject
import com.segment.analytics.kotlin.destinations.appsflyer.AppsFlyerDestination
import com.segment.analytics.plugins.DestinationFilters
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import java.util.concurrent.Executors

class MainApplication : Application() {
    companion object {
        lateinit var analytics: Analytics
    }

    override fun onCreate() {
        super.onCreate()

        analytics = Analytics(
            "tteOFND0bb5ugJfALOJWpF0wu1tcxYgr",
            applicationContext
        ) {
            this.collectDeviceId = true
            this.trackApplicationLifecycleEvents = true
            this.trackDeepLinks = true
            this.flushAt = 1
            this.flushInterval = 0
        }

//        analytics.add(WebhookPlugin("https://webhook.site/c6349c6a-bc14-49be-9677-0c8df3e07b58", Executors.newSingleThreadExecutor()))
        analytics.add(AppsFlyerDestination(applicationContext, false))

        val df = DestinationFilters()
        analytics.add(df)

        analytics.analyticsScope.launch {
            delay(5000L)
            analytics.settingsAsync()?.let { settings ->
                settings.middlewareSettings = updateJsonObject(settings.middlewareSettings) {
                    it["routingRules"] = Json.decodeFromString<JsonArray>("""
                    [{
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
                             "advertisingId",
                             "model",
                             "manufacturer"
                           ]
                         }
                       }
                     }
                   ]
                 ],
                 "destinationName": "Segment.io"
               }]
                """.trimIndent())
                }
                df.update(settings, Plugin.UpdateType.Initial)
            }
        }

    }

}