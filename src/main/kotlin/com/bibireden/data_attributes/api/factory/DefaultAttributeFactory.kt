package com.bibireden.data_attributes.api.factory

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.data.EntityTypeData
import net.minecraft.util.Identifier

/**
 * Meant to register attributes into the game's [AttributeConfigManager] directly.
 *
 * This is useful for mods that wish to implement their own defaults, so they can be applied to the world.
 * Ensure that it is not done through static initialization, the config is not guaranteed to exist at that time.
 *
 * Instead, register afterward, such as on **mod initialization**.
 */
object DefaultAttributeFactory {
    @Deprecated("Use the new system to register default entries.", level = DeprecationLevel.WARNING)
    @JvmStatic
    /** Registers default [AttributeOverride]'s to the config if they are not present currently within the config. */
    fun registerOverrides(overrides: Map<Identifier, AttributeOverride>) {
        val current = DataAttributes.OVERRIDES_CONFIG.overrides.toMutableMap()
        overrides.forEach { (id, ao) -> current.computeIfAbsent(id) { ao } }
        DataAttributes.OVERRIDES_CONFIG.overrides = current
        DataAttributes.OVERRIDES_CONFIG.save()
    }

    @Deprecated("Use the new system to register default entries.", level = DeprecationLevel.WARNING)
    @JvmStatic
    /** Registers default [AttributeFunction]'s to the config if they are not present currently within the config. */
    fun registerFunctions(functions: Map<Identifier, List<AttributeFunction>>) {
        val config = DataAttributes.FUNCTIONS_CONFIG
        val current = config.functions.data.toMutableMap()
        for ((id, af) in functions) {
            val currentFunctions = current.getOrPut(id) { listOf() }.toMutableList()
            // I made my own bed, now I have to sit in it for a bit...
            af.forEach {
                if (current[id]?.find { existing -> existing.id == it.id } == null) {
                    currentFunctions.add(it)
                }
            }
            current[id] = currentFunctions
        }
        DataAttributes.FUNCTIONS_CONFIG.functions.data = current
        DataAttributes.FUNCTIONS_CONFIG.save()
    }

    @Deprecated("Use the new system to register default entries.", level = DeprecationLevel.WARNING)
    @JvmStatic
    /** Registers default [EntityTypeData]'s to the config if they are not present currently within the config. */
    fun registerEntityTypes(entityTypes: Map<Identifier, EntityTypeData>) {
        val current = DataAttributes.ENTITY_TYPES_CONFIG.entity_types.toMutableMap()
        for ((id, type) in entityTypes) {
            val types = current.getOrPut(id) { EntityTypeData() }.data.toMutableMap()
            type.data.forEach { (typeID, value) ->
                if (!types.contains(typeID)) types[typeID] = value
            }
            current[id] = EntityTypeData(types)
        }
        DataAttributes.ENTITY_TYPES_CONFIG.entity_types = current
        DataAttributes.ENTITY_TYPES_CONFIG.save()
    }
}