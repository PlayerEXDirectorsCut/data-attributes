package com.bibireden.data_attributes.api

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*

typealias EntityAttributeSupplier = () -> Optional<EntityAttribute>

object DataAttributesAPI {
    @JvmField
    val MOD_ID = "data_attributes"

    /** Creates an identifier based on the API. */
    fun id(str: String) = Identifier.of(MOD_ID, str)!!

    /**
     * Gets a [Function] that will provide a registered attribute assigned to the given key.
     *
     * Static initialization is not possible for these attributes, which is the reason why this is handled differently.
     */
    fun getAttribute(key: Identifier): EntityAttributeSupplier {
        return { -> Optional.ofNullable(Registries.ATTRIBUTE[key]) }
    }

    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     *
     * The returned [Optional] provides more versatility.
     */
    fun getValue(entity: LivingEntity, supplier: EntityAttributeSupplier): Optional<Double> {
        val container = entity.attributes
        val attribute = supplier()

        if (!attribute.isEmpty && container.hasAttribute(attribute.get())) {
            return Optional.of(container.getValue(attribute.get()))
        }
        else {
            return Optional.empty()
        }
    }
}