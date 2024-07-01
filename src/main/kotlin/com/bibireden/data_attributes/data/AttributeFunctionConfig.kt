package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.endec.Endecs
import io.wispforest.endec.Endec
import net.minecraft.util.Identifier

/**
 * Container for data that applies modifiers to specific [EntityAttribute]'s based on a [AttributeFunction].
 */
data class AttributeFunctionConfig(var data: Map<Identifier, List<AttributeFunction>> = mapOf()) {
    companion object {
        val ENDEC = Endec.map(Endecs.IDENTIFIER, AttributeFunction.ENDEC.listOf())
            .xmap(::AttributeFunctionConfig) { it.data }
    }
}