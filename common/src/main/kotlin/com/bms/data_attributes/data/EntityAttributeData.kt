package com.bms.data_attributes.data

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.endec.Endecs
import com.bms.data_attributes.ext.keyOfMutable
import com.bms.data_attributes.mutable.MutableAttribute
import io.wispforest.endec.StructEndec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.resources.ResourceLocation

/**
 * Data that might contain a `AttributeOverrideConfig` and contains
 * `AttributeFunctions` to be used to override or copy to `Attributes`.
 */
class AttributeData(val override: AttributeOverride? = null, val functions: MutableMap<ResourceLocation, AttributeFunction> = mutableMapOf()) {
    constructor(value: AttributeOverride) : this(value, mutableMapOf())

    companion object {
        @JvmField
        val ENDEC: StructEndec<AttributeData> = StructEndecBuilder.of(
            AttributeOverride.ENDEC.nullableOf().fieldOf("override") { it.override },
            Endecs.RESOURCE.keyOfMutable(AttributeFunction.ENDEC).fieldOf("functions") { it.functions },
            ::AttributeData,
        )
    }

    /**
     * Sets the override for an [Attribute].
     *
     * This will completely overwrite any other override set and replace it with the one present in this class.
     * If there is none present, then no override will be done.
     */
    fun override(attribute: Attribute) {
        this.override?.override(attribute)
    }

    /** Copies to a provided [Attribute] by adding children to itself. */
    fun copy(attribute: Attribute) {
        with(attribute as MutableAttribute) {
            for ((id, function) in functions) {
                val childAttribute = BuiltInRegistries.ATTRIBUTE[id] as? MutableAttribute
                    ?: continue
                this.`data_attributes$addChild`(childAttribute, function)
            }
        }
    }
    
    /** Joins a [Map] of [AttributeFunction]'s with the data in this class. */
    fun putFunctions(functions: Map<ResourceLocation, AttributeFunction>) {
        for ((id, function) in functions) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                DataAttributes.LOGGER.warn("The attribute function child [$id] does not seem to be registered. This could allude to a missing mod or registered attribute.")
            }
            this.functions[id] = function
        }
    }
}