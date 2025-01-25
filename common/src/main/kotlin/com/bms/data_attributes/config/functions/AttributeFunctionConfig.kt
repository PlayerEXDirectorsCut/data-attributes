package com.bms.data_attributes.config.functions

import com.bms.data_attributes.endec.Endecs
import com.bms.data_attributes.ext.keyOf
import com.bms.data_attributes.ext.keyOfMutable
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.resources.ResourceLocation

/**
 * Container for data that applies modifiers to specific [Attribute]'s based on a [AttributeFunction].
 */
data class AttributeFunctionConfig(
    val data: MutableMap<ResourceLocation, MutableMap<ResourceLocation, AttributeFunction>> = mutableMapOf()
) {
    companion object {
        @JvmField
        val ENDEC = Endecs.RESOURCE.keyOfMutable(Endecs.RESOURCE.keyOfMutable(AttributeFunction.ENDEC)).xmap(::AttributeFunctionConfig) { it.data }
    }
}