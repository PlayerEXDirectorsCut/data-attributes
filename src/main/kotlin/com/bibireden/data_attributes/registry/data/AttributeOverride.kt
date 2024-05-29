package com.bibireden.data_attributes.registry.data

import com.bibireden.data_attributes.api.enums.StackingBehavior
import com.bibireden.data_attributes.api.enums.StackingFormula
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * An override for a specific `EntityAttribute`.
 *
 * As skills are **not** attributes, they are not meant to be overridden.
 * We are not meant to be creating an `EntityAttribute`, instead we are just doing an override of some depending on the data-pack(s).
 */
class AttributeOverride(val min: Double, val max: Double, val increment: Double, val behavior: StackingBehavior, val formula: StackingFormula) {
    companion object {
        val codec: Codec<AttributeOverride> = RecordCodecBuilder.create { builder ->
            builder.group(
                Codec.DOUBLE.fieldOf("min").forGetter { it.min },
                Codec.DOUBLE.fieldOf("max").forGetter { it.max },
                Codec.DOUBLE.fieldOf("increment").forGetter { it.increment },
                Codec.STRING.fieldOf("behavior").xmap(StackingBehavior::of) { it.uppercased }.forGetter { it.behavior },
                Codec.STRING.fieldOf("formula").xmap(StackingFormula::of) { it.uppercased }.forGetter { it.formula }
            ).apply(builder, ::AttributeOverride)
        }
    }
}