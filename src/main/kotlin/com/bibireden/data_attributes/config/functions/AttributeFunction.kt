package com.bibireden.data_attributes.config.functions

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.endec.Endecs
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.util.Identifier

/**
 * A function that is composed of the target [EntityAttribute] by an [Identifier] it will be applied to,
 * the [StackingBehavior] of the function which will determine if the given [getValue] will be an additive or multiplicative one.
 * */
data class AttributeFunction(var id: Identifier, var behavior: StackingBehavior, var value: Double) {
    @Suppress("UNUSED")
    constructor() : this(Identifier("unknown"), StackingBehavior.Add, 0.0)

    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            Endecs.IDENTIFIER.fieldOf("id") { it.id },
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )
    }
}
