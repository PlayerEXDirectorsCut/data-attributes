package com.bibireden.data_attributes.api.event

import com.bibireden.data_attributes.api.event.AttributesReloadedEvent.Reloaded
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

/**
 * Event that allows for logic reliant on config to be ordered after they are **loaded** on both the server and client.
 */
object AttributesReloadedEvent {
    /**
     * Triggered on the **server** upon these conditions:
     * - World Startup (if the config is enabled for it)
     * - Reload through `/reload` brigadier command.
     *
     * Triggered on the **client** upon these conditions:
     * - On sync with the server, primarily upon any join connection or reload.
     */
    @JvmField
    val EVENT: Event<Reloaded> = EventFactory.createArrayBacked(Reloaded::class.java) {
        Reloaded { it.forEach(Reloaded::onReloadCompleted) }
    }

    fun interface Reloaded {
        /** When the config data has fully been applied.  */
        fun onReloadCompleted()
    }
}