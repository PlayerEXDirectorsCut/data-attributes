package com.bibireden.data_attributes.registry.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

/**
 * Meant to just be a simple `value` that acts upon `EntityAttributes`, though it is **not** an entity attribute itself.
 *
 * These will need to be registered and indexed properly, which can happen through the `DynamicRegistry`.
 */
class Skill(val value: Double) {
    val codec: Codec<Skill> = RecordCodecBuilder.create { builder ->
        builder.group(Codec.DOUBLE.fieldOf("value").forGetter { it.value }).apply(builder, ::Skill)
    }
}