package com.bms.data_attributes.api.event

import com.bms.data_attributes.api.event.AttributeEvents.Reloaded
import io.wispforest.owo.util.EventSource
import io.wispforest.owo.util.EventStream

/**
 * Event that allows for logic reliant on config to be ordered after they are **loaded** on both the server and client.
 */
@Suppress("unused")
object AttributeEvents {
    /**
     * Triggered on the **server** upon these conditions:
     * - World Startup (if the config is enabled for it)
     * - Reload through `/reload` brigadier command.
     *
     * Triggered on the **client** upon these conditions:
     * - On sync with the server, primarily upon any join connection or reload.
     */
    @JvmField
    val RELOADED: EventSource<Reloaded> = Reloaded.stream.source()

    fun interface Reloaded {
        /** When the config data has fully been applied.  */
        fun onReloadCompleted()

        companion object {
            @JvmField
            val stream: EventStream<Reloaded> = EventStream { subscribers ->
                Reloaded { subscribers.forEach { it.onReloadCompleted() } }
            }
        }
    }
}