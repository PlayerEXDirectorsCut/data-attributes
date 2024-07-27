package com.bibireden.data_attributes.config.functions

import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.ext.keyOf
import io.wispforest.endec.Endec
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.util.Identifier

/**
 * Container for data that applies modifiers to specific [EntityAttribute]'s based on a [AttributeFunction].
 */
data class AttributeFunctionConfig(var data: Map<Identifier, List<AttributeFunction>> = mapOf()) {
    companion object {
        @JvmField
        val ENDEC = Endecs.IDENTIFIER.keyOf(AttributeFunction.ENDEC.listOf()).xmap(::AttributeFunctionConfig) { it.data }
    }
}