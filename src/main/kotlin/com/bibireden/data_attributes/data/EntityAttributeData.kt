package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.ext.keyOf
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
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
            Endecs.IDENTIFIER.keyOf(AttributeFunction.ENDEC).fieldOf("functions") { it.functions },
            ::EntityAttributeData,
        )
    }

    /**
     * Sets the override for an [EntityAttribute].
     *
     * This will completely overwrite any other override set and replace it with the one present in this class.
     * If there is none present, then no override will be done.
     */
    fun override(attribute: EntityAttribute) {
        this.override?.override(attribute)
    }

    /** Copies to a provided [EntityAttribute] by adding children to itself. */
    fun copy(attribute: EntityAttribute) {
        with(attribute as MutableEntityAttribute) {
            for ((id, function) in functions) {
                val childAttribute = Registries.ATTRIBUTE[id] as? MutableEntityAttribute ?: continue
                this.`data_attributes$addChild`(childAttribute, function)
            }
        }
    }
    
    /** Joins a [List] of [AttributeFunction]'s with the data in this class. */
    fun putFunctions(functions: List<AttributeFunction>) {
        this.functions.putAll(functions.map { (id, behavior, value) ->
            if (!Registries.ATTRIBUTE.containsId(id)) {
                DataAttributes.LOGGER.warn("The attribute function child [$id] does not seem to be registered. This could allude to a missing mod or registered attribute.")
            }
            id to AttributeFunction(id, behavior, value)
        })
    }
}