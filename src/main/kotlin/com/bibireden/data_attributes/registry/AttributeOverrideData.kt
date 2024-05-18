package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.api.enums.FunctionBehavior
import com.bibireden.data_attributes.api.enums.StackingFormula
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/** A segment of data pertaining to a specific attribute meant to be overrided in the game. */
class AttributeOverrideData(
    val default: Double,
    val min: Double,
    val max: Double,
    val increment: Double,
    private val behavior: FunctionBehavior,
    private val formula: StackingFormula
) {
    companion object {
        val CODEC: Codec<AttributeOverrideData> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.DOUBLE.fieldOf("default").forGetter { it.default },
                Codec.DOUBLE.fieldOf("min").forGetter { it.min },
                Codec.DOUBLE.fieldOf("max").forGetter { it.max },
                Codec.DOUBLE.fieldOf("increment").forGetter { it.increment },
                Codec.STRING.xmap(FunctionBehavior::of) { x -> x.name.uppercase() }.fieldOf("behavior").forGetter { it.behavior },
                Codec.STRING.xmap(StackingFormula::of) { x -> x.name.uppercase() }.fieldOf("formula").forGetter { it.formula },
            ).apply(builder, ::AttributeOverrideData)
        }
    }
}