package com.bibireden.data_attributes.api

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.DataAttributesClient
import com.bibireden.data_attributes.api.attribute.EntityAttributeSupplier
import com.bibireden.data_attributes.api.attribute.IEntityAttribute
import com.bibireden.data_attributes.config.AttributeConfigManager
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.world.World
import java.util.*
import java.util.function.Supplier

/**
 * The front-facing API for **DataAttributes**
 * that contains functions for fetching the current value of an attribute via
 * the direct [EntityAttribute] or the means of an [EntityAttributeSupplier] (or equivalent),
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
    fun getManager(world: World) = DataAttributes.getManagerFromWorld(world)

    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getValue(attribute: EntityAttribute, entity: LivingEntity): Optional<Double> {
        val container = entity.attributes

        return if (container.hasAttribute(attribute)) {
            Optional.of(container.getValue(attribute))
        }
        else {
            Optional.empty()
        }
    }

    /**
     * Tries to obtain a [EntityAttribute] formatted value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getValue(supplier: Supplier<Optional<EntityAttribute>>, entity: LivingEntity): Optional<Double> {
        return supplier.get().filter(entity.attributes::hasAttribute).map(entity.attributes::getValue)
    }

    /**
     * Tries to obtain a [EntityAttribute] formatted value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getFormattedValue(attribute: EntityAttribute, entity: LivingEntity): String {
        val attr = (attribute as IEntityAttribute)
        return attr.`data_attributes$format`().function(attr.`data_attributes$min`(), attr.`data_attributes$max`(), getValue(attribute, entity).orElse(0.0))
    }

    /**
     * Tries to obtain a [EntityAttribute] formatted value off a [LivingEntity] based on a supplier implementation.
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    @JvmStatic
    fun getFormattedValue(supplier: Supplier<Optional<EntityAttribute>>, entity: LivingEntity): String
    {
        return supplier.get().filter(entity.attributes::hasAttribute).map { getFormattedValue(it, entity) }.orElse("N/A")
    }
}