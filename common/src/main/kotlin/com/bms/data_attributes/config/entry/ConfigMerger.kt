package com.bms.data_attributes.config.entry

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.config.entities.EntityTypeEntry
import net.minecraft.resources.ResourceLocation

object ConfigMerger {
    fun mergeOverrides(values: Map<ResourceLocation, AttributeOverride>): Map<ResourceLocation, AttributeOverride> {
        val entries = values.toMutableMap()
        for ((id, override) in DataAttributes.OVERRIDES_CONFIG.entries) {
            entries[id] = override
        }
        return entries
    }

    fun mergeFunctions(values: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>): Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryEntry) in DataAttributes.FUNCTIONS_CONFIG.entries.data) {
            val secondaryEntry = entries[primaryId]?.toMutableMap()
            if (secondaryEntry == null) {
                entries[primaryId] = primaryEntry
            }
            else {
                for ((id, value) in primaryEntry) {
                    secondaryEntry[id] = value
                }
                entries[primaryId] = secondaryEntry
            }
        }
        return entries
    }

    fun mergeEntityTypes(values: Map<ResourceLocation, Map<ResourceLocation, EntityTypeEntry>>): Map<ResourceLocation, EntityTypeData> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryEntry) in DataAttributes.ENTITY_TYPES_CONFIG.entries) {
           val secondaryEntry = entries[primaryId]?.toMutableMap()
           if (secondaryEntry == null) {
               entries[primaryId] = primaryEntry.data
           }
           else {
               for ((id, value) in primaryEntry.data) {
                   secondaryEntry[id] = value
               }
               entries[primaryId] = secondaryEntry
           }
        }
        return entries.entries.associate { (k, v) -> k to EntityTypeData(v) }
    }
}