package com.bibireden.data_attributes.api.events

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier

class EntityAttributeModifiedEvents {
    companion object {
        val MODIFIED: Event<Modified> = EventFactory.createArrayBacked(Modified::class.java) { listeners -> Modified {
            attribute, livingEntity, modifier, prevValue, isAdded ->
            for (callback: Modified in listeners) {
                callback.onModified(attribute, livingEntity, modifier, prevValue, isAdded)
            }
        }}

        val CLAMPED: Event<Clamped> = EventFactory.createArrayBacked(Clamped::class.java) { listeners -> Clamped {
            attribute, value ->

            var cache: Double = value

            for (callback: Clamped in listeners) {
                cache = callback.onClamped(attribute, cache)
            }

            return@Clamped cache
        }}
    }

    fun interface Modified {
        fun onModified(attribute: EntityAttribute, livingEntity: LivingEntity?, modifier: EntityAttributeModifier?, prevValue: Double, isAdded: Boolean)
    }

    fun interface Clamped {
        fun onClamped(attribute: EntityAttribute, value: Double): Double
    }
}