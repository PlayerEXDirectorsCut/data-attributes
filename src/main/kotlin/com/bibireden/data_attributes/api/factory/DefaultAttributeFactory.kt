package com.bibireden.data_attributes.api.factory

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.data.EntityTypeData
import net.minecraft.util.Identifier

/**
 * Meant to register attributes into the DataAttributes config primarily after it is initialized.
 *
 * This is useful for mods that wish to implement their own defaults, so they can be applied to the world.
 * Ensure that it is not done through static initialization, the config is not guaranteed to exist at that time. Instead, register afterward, such as on **mod initialization**.
 */
object DefaultAttributeFactory {
    /** Registers default [AttributeOverride]'s to the config if they are not present currently within the config. */
    fun registerOverrides(overrides: Map<Identifier, AttributeOverride>) {
        val current = DataAttributes.OVERRIDES_CONFIG.overrides.toMutableMap()
        overrides.forEach { (id, ao) -> current.computeIfAbsent(id) { ao } }
        DataAttributes.OVERRIDES_CONFIG.overrides = current
        DataAttributes.OVERRIDES_CONFIG.save()
    }

    /** Registers default [AttributeFunction]'s to the config if they are not present currently within the config. */
    fun registerFunctions(functions: Map<Identifier, List<AttributeFunction>>) {
        val current = DataAttributes.FUNCTIONS_CONFIG.functions.data.toMutableMap()
        functions.forEach { (id, af) -> current.computeIfAbsent(id) { af } }
        DataAttributes.FUNCTIONS_CONFIG.functions.data = current
        DataAttributes.FUNCTIONS_CONFIG.save()
    }

    /** Registers default [EntityTypeData]'s to the config if they are not present currently within the config. */
    fun registerEntityTypes(entityTypes: Map<Identifier, EntityTypeData>) {
        val current = DataAttributes.ENTITY_TYPES_CONFIG.entity_types.toMutableMap()
        entityTypes.forEach { (id, types) -> current.computeIfAbsent(id) { types } }
        DataAttributes.ENTITY_TYPES_CONFIG.entity_types = current
        DataAttributes.ENTITY_TYPES_CONFIG.save()
    }
}