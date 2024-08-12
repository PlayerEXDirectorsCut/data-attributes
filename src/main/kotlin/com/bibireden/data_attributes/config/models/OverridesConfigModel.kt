package com.bibireden.data_attributes.config.models

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.api.attribute.AttributeFormat
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.*
import kotlinx.serialization.Serializable
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.util.Identifier

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/overrides", wrapperName = "OverridesConfig")
@Sync(SyncMode.NONE)
class OverridesConfigModel {
    @SectionHeader("overrides")

    @JvmField
    @Hook
    var overrides: Map<Identifier, AttributeOverride> = mapOf()

    @Serializable
    data class AttributeOverride(
        @JvmField
        var enabled: Boolean = true,
        @JvmField
        var min: Double = Double.NaN,
        @JvmField
        var max: Double = Double.NaN,
        @JvmField
        var smoothness: Double = 0.01,
        @JvmField
        var min_fallback: Double = 0.0,
        @JvmField
        var max_fallback: Double = 20.0,
        @JvmField
        var formula: StackingFormula = StackingFormula.Flat,
        @JvmField
        var format: AttributeFormat = AttributeFormat.Whole,
    ) {
        /** Calls an override of an `MutableEntityAttribute`. */
        fun override(attribute: EntityAttribute) {
            (attribute as MutableEntityAttribute).`data_attributes$override`(this)
        }

        companion object {
            @JvmField
            val ENDEC: Endec<AttributeOverride> = StructEndecBuilder.of(
                Endec.BOOLEAN.optionalFieldOf("enabled", { it.enabled }, false),
                Endec.DOUBLE.optionalFieldOf("min", { it.min }, 0.0),
                Endec.DOUBLE.optionalFieldOf("max", { it.max }, 1_000_000.0),
                Endec.DOUBLE.optionalFieldOf("smoothness", { it.smoothness }, 0.01),
                Endec.DOUBLE.optionalFieldOf("min_fallback", { it.min_fallback }, 0.0),
                Endec.DOUBLE.optionalFieldOf("max_fallback", { it.max_fallback }, 1_000_000.0),
                StackingFormula.ENDEC.optionalFieldOf("formula", { it.formula }, StackingFormula.Flat),
                AttributeFormat.ENDEC.optionalFieldOf("format", { it.format }, AttributeFormat.Whole),
                ::AttributeOverride,
            )
        }
    }
}

