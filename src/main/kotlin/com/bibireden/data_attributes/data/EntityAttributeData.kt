package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * Data that might contain a `AttributeOverrideConfig` and contains
 * `AttributeFunctions` to be used to override or copy to `EntityAttributes`.
 */
class EntityAttributeData(val override: AttributeOverride? = null, val functions: MutableMap<Identifier, AttributeFunction> = mutableMapOf()) {
    constructor(value: AttributeOverride) : this(value, mutableMapOf())

    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            AttributeOverride.ENDEC.nullableOf().fieldOf("override") { it.override },
            Endec.map(Endecs.IDENTIFIER, AttributeFunction.ENDEC).fieldOf("functions") { it.functions },
            ::EntityAttributeData,
        )
    }

    /** Overrides a `EntityAttribute`. */
    fun override(attribute: EntityAttribute) {
        this.override?.override(attribute as MutableEntityAttribute)
    }

    /** Copies to a given `EntityAttribute` by adding children to the instance via mixin. */
    fun copy(attributeIn: EntityAttribute) {
        val attribute = attributeIn as MutableEntityAttribute
        for ((id, function) in functions) {
            val childAttribute = Registries.ATTRIBUTE[id] ?: continue
            attribute.`data_attributes$addChild`(childAttribute as MutableEntityAttribute, function)
        }
    }

    fun putFunctions(functions: List<AttributeFunction>) {
        val mapping = mutableMapOf<Identifier, AttributeFunction>()
        functions.forEach { (id, behavior, value) ->
            if (!Registries.ATTRIBUTE.containsId(id)) {
                DataAttributes.LOGGER.warn("The attribute function child [$id] does not seem to be registered. This could allude to a missing mod or registered attribute.")
            }
            mapping[id] = AttributeFunction(id, behavior, value)
        }
        this.functions.putAll(mapping)
    }
}