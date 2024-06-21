package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * Data that might contain a `AttributeOverrideConfig` and contains
 * `AttributeFunctions` to be used to override or copy to `EntityAttributes`.
 */
class EntityAttributeData(val override: AttributeOverrideConfig? = null, val functions: MutableMap<Identifier, AttributeFunction> = mutableMapOf()) {
    constructor(value: AttributeOverrideConfig) : this(value, mutableMapOf())

    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            AttributeOverrideConfig.ENDEC.nullableOf().fieldOf("override") { it.override },
            Endec.map(CodecUtils.ofCodec(Identifier.CODEC), AttributeFunction.ENDEC).fieldOf("functions") { it.functions },
            ::EntityAttributeData,
        )
    }

    /** Overrides a `EntityAttribute`. */
    fun override(id: Identifier, fn: (id: Identifier, attribute: EntityAttribute) -> EntityAttribute) {
//        this.override?.override(fn(id, this.override.create()) as MutableEntityAttribute)
    }

    /** Copies to a given `EntityAttribute` by adding children to the instance via mixin. */
    fun copy(attributeIn: EntityAttribute) {
        val attribute = attributeIn as MutableEntityAttribute
        for ((id, function) in functions) {
            val childAttribute = Registries.ATTRIBUTE[id] ?: continue
            attribute.`data_attributes$addChild`(childAttribute as MutableEntityAttribute, function)
        }
    }

    fun putFunctions(functions: Map<Identifier, AttributeFunction>) {
        this.functions.putAll(functions)
    }
}