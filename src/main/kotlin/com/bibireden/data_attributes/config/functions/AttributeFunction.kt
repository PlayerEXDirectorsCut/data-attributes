package com.bibireden.data_attributes.config.functions

import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.api.attribute.AttributeFormat
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.serde.IdentifierSerializer
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import kotlinx.serialization.Serializable
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.util.Identifier

/**
 * A function that is composed of the target [EntityAttribute] by an [Identifier] it will be applied to,
 * the [StackingBehavior] of the function which will determine if the given [getValue] will be an additive or multiplicative one.
 * */
@Serializable
data class AttributeFunction(
    var enabled: Boolean,
    var behavior: StackingBehavior,
    var value: Double
) {
    @Suppress("UNUSED")
    constructor() : this(true, StackingBehavior.Add, 0.0)

    companion object {
        @JvmField
        val ENDEC = StructEndecBuilder.of(
            Endec.BOOLEAN.optionalFieldOf("enabled", { it.enabled }, true),
            Endec.STRING.xmap(StackingBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior") { it.behavior },
            Endec.DOUBLE.fieldOf("value") { it.value },
            ::AttributeFunction
        )
    }
}
