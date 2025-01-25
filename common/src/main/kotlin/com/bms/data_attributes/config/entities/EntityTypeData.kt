@file:UseSerializers(ResourceLocationSerializer::class)

package com.bms.data_attributes.config.entities

import com.bms.data_attributes.endec.Endecs
import com.bms.data_attributes.ext.keyOf
import com.bms.data_attributes.mutable.MutableAttributeSupplier
import com.bms.data_attributes.serde.ResourceLocationSerializer
import io.wispforest.endec.Endec
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import kotlin.jvm.optionals.getOrNull

/**
 * Container for data that modifies the specific [BuiltInRegistries.ENTITY_TYPE] entry with an associated [EntityTypeEntry].
 */
@Serializable
data class EntityTypeData(val data: Map<ResourceLocation, EntityTypeEntry> = mapOf()) {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypeData> = Endecs.RESOURCE.keyOf(EntityTypeEntry.ENDEC).xmap(::EntityTypeData) { it.data }
    }

    /**
     * Builds the entity-type data with a provided [AttributeSupplier.Builder],
     * along with an optional [DefaultAttributeContainer].
     */
    fun build(builder: AttributeSupplier.Builder, supplier: AttributeSupplier?) {
        (supplier as MutableAttributeSupplier).`data_attributes$copy`(builder)
        for ((key, entry) in this.data) {
            val attribute = BuiltInRegistries.ATTRIBUTE.getHolder(key).getOrNull() ?: continue
            builder.add(attribute, attribute.value().sanitizeValue(entry.value))
        }
    }
}