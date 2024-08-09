package com.bibireden.data_attributes.config.entry

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.data.EntityTypeData
import net.minecraft.util.Identifier

object ConfigMerger {
    fun mergeOverrides(values: Map<Identifier, AttributeOverride>): Map<Identifier, AttributeOverride> {
        val entries = values.toMutableMap()
        for ((id, override) in DataAttributes.OVERRIDES_CONFIG.overrides) {
            entries[id] = override
        }
        return entries
    }

    fun mergeFunctions(values: Map<Identifier, List<AttributeFunction>>): Map<Identifier, List<AttributeFunction>> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryFunctions) in DataAttributes.FUNCTIONS_CONFIG.functions.data) {
            val entriesMap = entries[primaryId]
            if (entriesMap == null) {
                entries[primaryId] = primaryFunctions
            }
            else {
                val secondaryEntry = entriesMap.toMutableList()
                primaryFunctions.forEach { primaryFunction ->
                    var replaced = false
                    entriesMap.forEachIndexed { index, entry ->
                        if (entry.id == primaryFunction.id) {
                            secondaryEntry.removeAt(index)
                            secondaryEntry.add(index, primaryFunction)
                            replaced = true
                        }
                    }
                    if (!replaced) {
                        secondaryEntry.add(primaryFunction)
                    }
                }
                entries[primaryId] = secondaryEntry
            }
        }
        return entries
    }

    fun mergeEntityTypes(values: Map<Identifier, Map<Identifier, Double>>): Map<Identifier, EntityTypeData> {
        val entries = values.toMutableMap()
        for ((primaryId, primaryEntry) in DataAttributes.ENTITY_TYPES_CONFIG.entity_types) {
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