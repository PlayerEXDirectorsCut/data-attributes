package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.attribute.IAttributeFunction
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.util.Maths
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder

/** Represents a function normally tied to an `Identifier` that contains a `StackingBehavior` and a `Double` value. */
class AttributeFunction(val behavior: StackingBehavior, val value: Double) : IAttributeFunction {
    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )
    }

    override fun behavior(): StackingBehavior = this.behavior

    override fun value(): Double = this.value
}