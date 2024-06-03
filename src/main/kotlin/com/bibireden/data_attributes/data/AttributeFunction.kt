package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder

class AttributeFunction(val behavior: StackingBehavior, val value: Double) {
    companion object {
        val endec = StructEndecBuilder.of(
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )
    }
}