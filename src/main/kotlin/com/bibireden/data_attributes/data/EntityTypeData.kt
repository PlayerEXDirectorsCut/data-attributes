package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

data class EntityTypeData(val data: MutableMap<Identifier, Double> = mutableMapOf()) {
    companion object {
        @JvmField
        val ENDEC: Endec<EntityTypeData> = Endec.map(Endecs.IDENTIFIER, Endec.DOUBLE).xmap(::EntityTypeData) { it.data }
    }

    fun build(builder: DefaultAttributeContainer.Builder, container: DefaultAttributeContainer?) {
        if (container != null) (container as MutableDefaultAttributeContainer).`data_attributes$copy`(builder)
        for ((key, value) in this.data) {
            val attribute = Registries.ATTRIBUTE[key] ?: continue
            builder.add(attribute, attribute.clamp(value))
        }
    }
}