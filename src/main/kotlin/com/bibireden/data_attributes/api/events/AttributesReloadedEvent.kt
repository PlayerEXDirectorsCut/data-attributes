package com.bibireden.data_attributes.api.events

import net.fabricmc.fabric.api.event.EventFactory

object AttributesReloadedEvent {
    val EVENT = EventFactory.createArrayBacked(Reloaded::class.java) { listeners -> Reloaded { ->
        listeners.forEach(Reloaded::onReload)
    }}

    fun interface Reloaded {
        fun onReload()
    }
}