package com.bms.data_attributes.api.event

import com.bms.data_attributes.api.event.AttributeModifiedEvents.Clamped
import com.bms.data_attributes.api.event.AttributeModifiedEvents.Modified
import io.wispforest.owo.util.EventSource
import io.wispforest.owo.util.EventStream
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

/**
 * Holds some attribute events. Both occur on both server and client.
 */
object AttributeModifiedEvents {
    /**
     * Fired when the value of an attribute instance was modified, either by adding/removing a modifier or changing
     * the value of the modifier, or by reloading the datapack and having the living entity renew its attribute container.
     * Living entity and modifiers may or may not be null.
     */
    @JvmField
    val MODIFIED: EventSource<Modified> = Modified.stream.source()

    /**
     * Fired after the attribute instance value was calculated, but before it was output. This offers one last chance to alter the
     * value in some way (for example, rounding a decimal to an integer).
     */
    @JvmField
    val CLAMPED: EventSource<Clamped> = Clamped.stream.source()

    fun interface Modified {
        fun onModified(attribute: Attribute?, livingEntity: LivingEntity?, modifier: AttributeModifier?, prevValue: Double, isWasAdded: Boolean)

        companion object {
            @JvmField
            val stream: EventStream<Modified> = EventStream { subscribers ->
                Modified { attribute, entity, modifier, prev, isWas -> subscribers.forEach { it.onModified(attribute, entity, modifier, prev, isWas) } }
            }
        }
    }

    fun interface Clamped {
        fun onClamped(attribute: Attribute?, value: Double): Double

        companion object {
            @JvmField
            val stream: EventStream<Clamped> = EventStream { subscribers ->
                Clamped { attribute, value ->
                    var current = value
                    subscribers.forEach {
                        current = it.onClamped(attribute, current)
                    }
                    current
                }
            }
        }
    }
}