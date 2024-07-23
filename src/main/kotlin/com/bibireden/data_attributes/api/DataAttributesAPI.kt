package com.bibireden.data_attributes.api

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel
import com.bibireden.data_attributes.data.EntityTypeData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import java.util.*
import java.util.function.Supplier

object DataAttributesAPI {
    @JvmStatic
    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity].
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
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

    @JvmStatic
    /**
     * Tries to obtain a [EntityAttribute] value off a [LivingEntity] based on a supplier implementation.
     * Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     */
    fun getValue(supplier: Supplier<EntityAttribute?>, entity: LivingEntity): Optional<Double> {
        val container = entity.attributes
        val attribute = supplier.get()

        return if (attribute != null && container.hasAttribute(attribute)) {
            Optional.of(container.getValue(attribute))
        }
        else {
            Optional.empty()
        }
    }

    @JvmStatic
    fun getOverride(attribute: EntityAttribute): OverridesConfigModel.AttributeOverride? {
        return DataAttributes.OVERRIDES_CONFIG.overrides[Registries.ATTRIBUTE.getId(attribute)]
    }

    @JvmStatic
    fun getFunctions(attribute: EntityAttribute): List<AttributeFunction>? {
        return DataAttributes.FUNCTIONS_CONFIG.functions.data[Registries.ATTRIBUTE.getId(attribute)]
    }

    @JvmStatic
    fun getEntityTypes(attribute: EntityAttribute): EntityTypeData? {
        return DataAttributes.ENTITY_TYPES_CONFIG.entity_types[Registries.ATTRIBUTE.getId(attribute)]
    }
}