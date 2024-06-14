package com.bibireden.data_attributes.api

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*

/**
 * Meant to act as a lazily fetched [EntityAttribute] through its [get] function.
 */
class EntityAttributeSupplier(val id: Identifier) {
    fun get(): Optional<EntityAttribute> = Optional.ofNullable(Registries.ATTRIBUTE[this.id])

    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     *
     * The returned [Optional] provides more versatility.
    */
    fun getValue(entity: LivingEntity): Optional<Double> {
        val container = entity.attributes
        val attribute = this.get()

        if (!attribute.isEmpty && container.hasAttribute(attribute.get())) {
            return Optional.of(container.getValue(attribute.get()))
        }
        else {
            return Optional.empty()
        }
    }
}