package com.segment.analytics.destination.filters.app

import android.app.Application
import com.segment.analytics.destination.filters.app.filters.WebhookPlugin
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.plugins.DestinationFilters
import java.util.concurrent.Executors

class MainApplication : Application() {
    companion object {
        lateinit var analytics: Analytics
    }

    override fun onCreate() {
        super.onCreate()

        analytics = Analytics(
            "93EMLzmXzP6EJ3cJOhdaAgEVNnZjwRqA",
            applicationContext
        ) {
            this.collectDeviceId = true
            this.trackApplicationLifecycleEvents = true
            this.trackDeepLinks = true
            this.flushAt = 1
            this.flushInterval = 0
        }

        analytics.add(WebhookPlugin("https://webhook.site/dbfde56b-361f-46c1-966d-9de9e047255f", Executors.newSingleThreadExecutor()))

        analytics.add(DestinationFilters())
    }

}