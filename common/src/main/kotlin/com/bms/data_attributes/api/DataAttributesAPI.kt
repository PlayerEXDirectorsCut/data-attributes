package com.bms.data_attributes.api

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.DataAttributesClient
import com.bms.data_attributes.api.attribute.AttributeSupplier
import com.bms.data_attributes.api.attribute.IAttribute
import com.bms.data_attributes.config.AttributeConfigManager
import net.minecraft.core.Holder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.level.Level
import java.util.*
import java.util.function.Supplier

/**
 * The front-facing API for **DataAttributes**
 * that contains functions for fetching the current value of an attribute via
 * the direct [Attribute] or the means of an [AttributeSupplier] (or equivalent),
 * and contains manager fields and functions.
 * */
object DataAttributesAPI {
    /** The [AttributeConfigManager] that is managed on the client. */
    @JvmStatic
    val clientManager: AttributeConfigManager = DataAttributesClient.MANAGER

    /** The [AttributeConfigManager] that is managed primarily by the server (dedicated or integrated). */
    @JvmStatic
    val serverManager: AttributeConfigManager = DataAttributes.MANAGER

    /**
     * Gets the [AttributeConfigManager] based on a provided world.
     * Depending on the context of that world (client or server), it will provide the proper one.
     */
    @JvmStatic
    fun getManager(level: Level) = DataAttributes.getManagerFromLevel(level)

    /**
     * Tries to obtain a [Attribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getValue(attribute: Holder<Attribute>, entity: LivingEntity): Optional<Double> {
        val container = entity.attributes

        return if (container.hasAttribute(attribute)) {
            Optional.of(container.getValue(attribute))
        }
        else {
            Optional.empty()
        }
    }

    /**
     * Tries to obtain a [Attribute] formatted value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getValue(supplier: Supplier<Optional<Holder<Attribute>>>, entity: LivingEntity): Optional<Double> {
        return supplier.get().filter(entity.attributes::hasAttribute).map(entity.attributes::getValue)
    }

    /**
     * Tries to obtain a [Attribute] formatted value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getFormattedValue(attribute: Holder<Attribute>, entity: LivingEntity): String {
        val attr = (attribute as IAttribute)
        return attr.`data_attributes$format`().function(attr.`data_attributes$min`(), attr.`data_attributes$max`(), getValue(attribute, entity).orElse(0.0))
    }

    /**
     * Tries to obtain a [Attribute] formatted value off a [LivingEntity] based on a supplier implementation.
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getFormattedValue(supplier: Supplier<Optional<Holder<Attribute>>>, entity: LivingEntity): String
    {
        return supplier.get().filter(entity.attributes::hasAttribute).map { getFormattedValue(it, entity) }.orElse("N/A")
    }
}