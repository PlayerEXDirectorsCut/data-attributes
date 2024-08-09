@file:UseSerializers(IdentifierSerializer::class)

package com.bibireden.data_attributes.data

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
 * Container for data that modifies the specific [Registries.ENTITY_TYPE] entry with an associated base value.
 */
@Serializable
data class EntityTypeData(val data: Map<Identifier, Double> = mapOf()) {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypeData> = Endecs.IDENTIFIER.keyOf(Endec.DOUBLE).xmap(::EntityTypeData) { it.data }
    }

    /**
     * Builds the entity-type data with a provided [DefaultAttributeContainer.Builder],
     * along with an optional [DefaultAttributeContainer].
     */
    fun build(builder: DefaultAttributeContainer.Builder, container: DefaultAttributeContainer?) {
        (container as MutableDefaultAttributeContainer).`data_attributes$copy`(builder)
        for ((key, value) in this.data) {
            val attribute = Registries.ATTRIBUTE[key] ?: continue
            builder.add(attribute, attribute.clamp(value))
        }
    }
}