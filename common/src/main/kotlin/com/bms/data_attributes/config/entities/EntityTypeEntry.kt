package com.bms.data_attributes.config.entities

import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import kotlinx.serialization.Serializable

/**
 * Inner structure for [EntityTypeData] that contains the current base value and a former value
 * (if the attribute was attached to this entity before).
 */
@Serializable
data class EntityTypeEntry(var value: Double = 0.0, var fallback: Double? = null) {
    companion object {
        val ENDEC: Endec<EntityTypeEntry> = StructEndecBuilder.of(
            Endec.DOUBLE.fieldOf("value") { it.value },
            Endec.DOUBLE.nullableOf().fieldOf("fallback") { it.fallback },
            ::EntityTypeEntry
        )
    }
}