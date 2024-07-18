package com.bibireden.data_attributes.config.models

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.config.ConfigDefaults
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.*;
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.util.Identifier

@Suppress("UNUSED")
@Config(name = "${DataAttributes.MOD_ID}/overrides", wrapperName = "OverridesConfig")
@Sync(SyncMode.NONE)
class OverridesConfigModel {
    @SectionHeader("overrides")

    @JvmField
    @Hook
    var overrides: Map<Identifier, AttributeOverride> = ConfigDefaults.OVERRIDES

    data class AttributeOverride(
        @JvmField
        var enabled: Boolean = true,
        @JvmField
        var min: Double = 0.0,
        @JvmField
        var max: Double = 1000.0,
        @JvmField
        var smoothness: Double = 0.0,
        @JvmField
        var min_fallback: Double = 0.0,
        @JvmField
        var max_fallback: Double = 20.0,
        @JvmField
        var formula: StackingFormula = StackingFormula.Flat
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
                Endec.DOUBLE.optionalFieldOf("max", { it.max }, ConfigDefaults.MAX_DOUBLE),
                Endec.DOUBLE.optionalFieldOf("smoothness", { it.smoothness }, 0.001),
                Endec.DOUBLE.optionalFieldOf("min_fallback", { it.min_fallback }, 0.0),
                Endec.DOUBLE.optionalFieldOf("max_fallback", { it.max_fallback }, ConfigDefaults.MAX_DOUBLE),
                Endec.STRING.xmap(StackingFormula::of) { it.name }.fieldOf("formula") { it.formula },
                OverridesConfigModel::AttributeOverride,
            )
        }
    }
}

