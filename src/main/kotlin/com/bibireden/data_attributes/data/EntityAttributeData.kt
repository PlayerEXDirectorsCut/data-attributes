package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * Data that might contain a `AttributeOverride` and contains
 * `AttributeFunctions` to be used to override or copy to `EntityAttributes`.
 */
class EntityAttributeData(val override: AttributeOverride? = null, val functions: MutableMap<Identifier, AttributeFunction> = mutableMapOf()) {
    constructor(value: AttributeOverride) : this(value, mutableMapOf())

    companion object {
        val endec = StructEndecBuilder.of(
            AttributeOverride.endec.optionalFieldOf("override", { it.override }, { -> null}),
            Endec.map(CodecUtils.ofCodec(Identifier.CODEC), AttributeFunction.endec).fieldOf("functions") { it.functions },
            ::EntityAttributeData,
        )
    }

    /** Overrides a `EntityAttribute`. */
    fun override(id: Identifier, fn: (id: Identifier, attribute: EntityAttribute?) -> EntityAttribute) {
        this.override?.override(fn(id, this.override.create()) as MutableEntityAttribute)
    }

    /** Copies to a given `EntityAttribute` by adding children to the instance via mixin. */
    fun copy(attributeIn: EntityAttribute) {
        val attribute = attributeIn as MutableEntityAttribute
        this.functions.keys.forEach {
            attribute.`data_attributes$addChild`(
                Registries.ATTRIBUTE[it] as? MutableEntityAttribute ?: return@forEach,
                this.functions[it] ?: return@forEach
            )
        }
    }

    fun putFunctions(functions: Map<Identifier, AttributeFunction>) {
        this.functions.putAll(functions)
    }
}