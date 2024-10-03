package com.bibireden.data_attributes.config.entry

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.config.entities.EntityTypeEntry
import net.minecraft.util.Identifier

object ConfigMerger {
    fun mergeOverrides(values: Map<Identifier, AttributeOverride>): Map<Identifier, AttributeOverride> {
        val entries = values.toMutableMap()
        for ((id, override) in DataAttributes.OVERRIDES_CONFIG.entries) {
            entries[id] = override
        }
        return entries
    }

    fun mergeFunctions(values: Map<Identifier, Map<Identifier, AttributeFunction>>): Map<Identifier, Map<Identifier, AttributeFunction>> {
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

    fun mergeEntityTypes(values: Map<Identifier, Map<Identifier, EntityTypeEntry>>): Map<Identifier, EntityTypeData> {
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