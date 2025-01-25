package com.bms.data_attributes.config.models

import blue.endless.jankson.Comment
import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.api.attribute.StackingFormula
import com.bms.data_attributes.api.attribute.AttributeFormat
import com.bms.data_attributes.mutable.MutableAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.*
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/overrides", wrapperName = "OverridesConfig")
@Sync(SyncMode.NONE)
class OverridesConfigModel {
    @SectionHeader("overrides")

    @JvmField
    @Hook
    @Comment("attribute overrides can change the range of the attribute, and can apply different formulas to modify its behavior when computed.")
    var entries: Map<ResourceLocation, AttributeOverride> = mapOf()

    @Serializable
    data class AttributeOverride(
        @JvmField
        var enabled: Boolean = true,
        @JvmField
        var min: Double = Double.NaN,
        @JvmField
        var max: Double = Double.NaN,
        @JvmField
        var smoothness: Double = 1.0,
        @JvmField
        var min_fallback: Double = 0.0,
        @JvmField
        var max_fallback: Double = 20.0,
        @JvmField
        var formula: StackingFormula = StackingFormula.Flat,
        @JvmField
        var format: AttributeFormat = AttributeFormat.Whole,
    ) {
        /** Calls an override of an `MutableAttribute`. */
        fun override(attribute: Attribute) {
            (attribute as MutableAttribute).`data_attributes$override`(this)
        }

        companion object {
            @JvmField
            val ENDEC: Endec<AttributeOverride> = StructEndecBuilder.of(
                Endec.BOOLEAN.optionalFieldOf("enabled", { it.enabled }, true),
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

