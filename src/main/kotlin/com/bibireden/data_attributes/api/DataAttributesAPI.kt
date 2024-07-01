package com.bibireden.data_attributes.api

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import java.util.*

object DataAttributesAPI {
    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     *
     * The returned [Optional] provides more versatility.
     */
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
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity] based on a supplier implementation.
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     *
     * The returned [Optional] provides more versatility.
     */
    fun getValue(supplier: () -> EntityAttribute?, entity: LivingEntity): Optional<Double> {
        val container = entity.attributes
        val attribute = supplier()

        return if (attribute != null && container.hasAttribute(attribute)) {
            Optional.of(container.getValue(attribute))
        }
        else {
            Optional.empty()
        }
    }
}