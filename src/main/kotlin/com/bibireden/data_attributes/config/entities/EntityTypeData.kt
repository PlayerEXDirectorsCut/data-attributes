@file:UseSerializers(IdentifierSerializer::class)

package com.bibireden.data_attributes.config.entities

import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.ext.keyOf
import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer
import com.bibireden.data_attributes.serde.IdentifierSerializer
import io.wispforest.endec.Endec
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * Container for data that modifies the specific [Registries.ENTITY_TYPE] entry with an associated [EntityTypeEntry].
 */
@Serializable
data class EntityTypeData(val data: Map<Identifier, EntityTypeEntry> = mapOf()) {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypeData> = Endecs.IDENTIFIER.keyOf(EntityTypeEntry.ENDEC).xmap(::EntityTypeData) { it.data }
    }

    /**
     * Builds the entity-type data with a provided [DefaultAttributeContainer.Builder],
     * along with an optional [DefaultAttributeContainer].
     */
    fun build(builder: DefaultAttributeContainer.Builder, container: DefaultAttributeContainer?) {
        (container as MutableDefaultAttributeContainer).`data_attributes$copy`(builder)
        for ((key, entry) in this.data) {
            val attribute = Registries.ATTRIBUTE[key] ?: continue
            builder.add(attribute, attribute.clamp(entry.value))
        }
    }
}