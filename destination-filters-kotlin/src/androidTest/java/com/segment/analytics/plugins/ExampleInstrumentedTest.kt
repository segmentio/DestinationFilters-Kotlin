package com.segment.analytics.plugins

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.segment.analytics.kotlin.android.Analytics
import com.segment.analytics.kotlin.core.AliasEvent
import com.segment.analytics.kotlin.core.BaseEvent
import com.segment.analytics.kotlin.core.GroupEvent
import com.segment.analytics.kotlin.core.IdentifyEvent
import com.segment.analytics.kotlin.core.ScreenEvent
import com.segment.analytics.kotlin.core.TrackEvent
import com.segment.analytics.kotlin.core.utilities.LenientJson
import com.segment.analytics.kotlin.core.utilities.getString
import com.segment.analytics.liveplugins.kotlin.LivePlugins
import com.segment.analytics.substrata.kotlin.JSObject
import com.segment.analytics.substrata.kotlin.JsonElementConverter
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class DestinationFiltersTest {

    val plugin = DestinationFilters().also {
//        it.engine = engine
    }

    @Test
    fun testSetup() {
        val analytics = Analytics("123", InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
        val edgeFnPlugin = LivePlugins()
        edgeFnPlugin.setup(analytics)
        plugin.setup(analytics)

        // Check engine init
        val engine = plugin.engine
        assertNotNull(engine)

        // Test if tsub is loaded
        val tsubLoaded = engine.await {
            return@await evaluate("""(typeof dest_filters.evaluateDestinationFilters === "function")""")
        }
        assertEquals(true, tsubLoaded)

        // Test if createDestinationFilter exists and works
        val result = engine.await(global = true) {
            evaluate(
                """
                |dest_filters.evaluateDestinationFilters(
                |   {
                |     "matchers": [
                |       {
                |         "ir": "",
                |         "type": "all"
                |       }
                |     ],
                |     "scope": "destinations",
                |     "target_type": "workspace::project::destination::config",
                |     "transformers": [
                |       [
                |         {
                |           "type": "drop_properties",
                |           "config": {
                |             "drop": {
                |               "context.device": [
                |                 "id",
                |                 "advertisingId"
                |               ]
                |             }
                |           }
                |         }
                |       ]
                |     ],
                |     "destinationName": "doesnt matter"
                |   },
                |   {
                |     "anonymousId": "fdae402a-dd8a-4117-9a9b-3aca3a15c36c",
                |     "context": {
                |       "app": {
                |         "name": "destination-filters-kotlin",
                |         "namespace": "com.segment.analytics.destination.filters.app",
                |         "version": "1.0"
                |       },
                |       "device": {
                |         "id": "bef8e59bc1db1cc7b80afa4c5bfad93c770bd01a7b10d770fc1dce7fe9864856",
                |         "manufacturer": "Google",
                |         "model": "Android SDK built for x86",
                |         "name": "generic_x86",
                |         "type": "android"
                |       }
                |     },
                |     "integrations": {},
                |     "event": "Application Opened",
                |     "messageId": "7ce02e33-a2c4-4b47-bced-8dda01f7b5b2",
                |     "timestamp": "2022-08-22T20:33:34.908Z",
                |     "properties": {
                |       "version": "1.0"
                |     },
                |     "type": "track",
                |     "writeKey": "93EMLzmXzP6EJ3cJOhdaAgEVNnZjwRqA"
                |   }
                |)
                |""".trimMargin()
        )}
        assertTrue(result is JSObject)
        engine.sync {
            JsonElementConverter.read(result).jsonObject.toBaseEvent().let {
                assertNotNull(it)
                assertEquals(4, it!!.context["device"]?.jsonObject?.size)
            }
        }



        // Test if update creates filters

        // Run an event through it all
    }


    private fun JsonObject.toBaseEvent(): BaseEvent? {
        val type = getString("type")

        return when (type) {
            "identify" -> LenientJson.decodeFromJsonElement(IdentifyEvent.serializer(), this)
            "track" -> LenientJson.decodeFromJsonElement(TrackEvent.serializer(), this)
            "screen" -> LenientJson.decodeFromJsonElement(ScreenEvent.serializer(), this)
            "group" -> LenientJson.decodeFromJsonElement(GroupEvent.serializer(), this)
            "alias" -> LenientJson.decodeFromJsonElement(AliasEvent.serializer(), this)
            else -> null
        }
    }
}