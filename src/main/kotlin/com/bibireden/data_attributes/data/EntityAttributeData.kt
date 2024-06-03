package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.json.AttributeFunctionJson
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class EntityAttributeData(val override: AttributeOverride, val functions: MutableMap<Identifier, AttributeFunction> = mutableMapOf()) {
    companion object {
        val endec = StructEndecBuilder.of(
            AttributeOverride.endec.fieldOf("override") { it.override },
            Endec.map(CodecUtils.ofCodec(Identifier.CODEC), AttributeFunction.endec).fieldOf("functions") { it.functions },
            ::EntityAttributeData,
        )
    }

    /** Overrides a `EntityAttribute`. */
    fun override(id: Identifier, fn: (id: Identifier, attribute: EntityAttribute?) -> EntityAttribute) {
        this.override.override(fn(id, this.override.create()) as MutableEntityAttribute)
    }

    fun copy(attributeIn: EntityAttribute) {
        val attribute = attributeIn as MutableEntityAttribute
        this.functions.keys.forEach {
            attribute.addChild(
                Registries.ATTRIBUTE[it] as? MutableEntityAttribute ?: return@forEach,
                this.functions[it] ?: return@forEach
            )
        }
    }

    fun putFunctions(functions: Map<Identifier, AttributeFunctionJson>) {
        this.functions.putAll(functions)
    }
}