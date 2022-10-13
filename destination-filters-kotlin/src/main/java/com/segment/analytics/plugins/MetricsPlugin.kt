package com.segment.analytics.plugins

import com.segment.analytics.kotlin.core.Analytics
import com.segment.analytics.kotlin.core.BaseEvent
import com.segment.analytics.kotlin.core.platform.Plugin
import com.segment.analytics.kotlin.core.utilities.putInContextUnderKey
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MetricsPlugin(setOfActiveDestinations: Set<String>): Plugin {

    private val activeDestinations: JsonArray

    init {
        activeDestinations = buildJsonArray {
            setOfActiveDestinations.forEach { add(it) }
        }
    }

    override val type: Plugin.Type = Plugin.Type.Enrichment
    override lateinit var analytics: Analytics
    override fun execute(event: BaseEvent): BaseEvent? {
        event.putInContextUnderKey(
            "plugins",
            "destination-filters",
            buildJsonObject {
                put("version", DestinationFilters.version)
                put("active", activeDestinations)
            }
        )
        return super.execute(event)
    }
}