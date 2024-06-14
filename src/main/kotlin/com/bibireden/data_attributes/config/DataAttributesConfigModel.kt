package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.api.attribute.StackingBehavior
import com.bibireden.data_attributes.data.AttributeFunction
import com.bibireden.data_attributes.data.AttributeFunctionConfig
import com.bibireden.data_attributes.data.AttributeFunctionConfigData
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.config.Option.SyncMode
import io.wispforest.owo.config.annotation.*;
import net.minecraft.util.Identifier

@Suppress("UNUSED")
@Config(name = DataAttributes.MOD_ID, wrapperName = "DataAttributesConfig")
@Sync(SyncMode.NONE)
class DataAttributesConfigModel {
    enum class StackingFormula { Flat, Diminished;
        companion object {
            fun of(str: String): StackingFormula {
                return when (str.uppercase()) {
                    "FLAT" -> Flat
                    else -> Diminished
                }
            }
        }
    }

    @SectionHeader("overrides")

    @JvmField
    @Hook
    var overrides: Map<Identifier, AttributeOverrideConfig> = mapOf()

    @SectionHeader("functions")

    @JvmField
    @Hook
    var functions: AttributeFunctionConfigData = AttributeFunctionConfigData(mapOf(
        Identifier("ktor") to listOf(
            AttributeFunctionConfig(Identifier("bloodshed"), StackingBehavior.Add, 29.0)
        )
    ))

    data class AttributeOverrideConfig(
        @JvmField
        var enabled: Boolean = true,
        @JvmField
        var min_fallback: Double = 0.0,
        @JvmField
        var max_fallback: Double = 20.0,
        @JvmField
        var min: Double = 0.0,
        @JvmField
        var max: Double = 1000.0,
        @JvmField
        var smoothness: Double = 0.0,
        @JvmField
        var formula: StackingFormula = StackingFormula.Flat
    ) {
        companion object {
            @JvmField
            val ENDEC: Endec<AttributeOverrideConfig> = StructEndecBuilder.of(
                Endec.BOOLEAN.optionalFieldOf("enabled", { it.enabled }, false),
                Endec.DOUBLE.optionalFieldOf("max_fallback", { it.max_fallback }, 1_000_000.0),
                Endec.DOUBLE.optionalFieldOf("min_fallback", { it.min_fallback }, 0.0),
                Endec.DOUBLE.optionalFieldOf("min", { it.min }, 0.0),
                Endec.DOUBLE.optionalFieldOf("max", { it.max }, 1_000_000.0),
                Endec.DOUBLE.optionalFieldOf("smoothness", { it.smoothness }, 0.0),
                Endec.STRING.xmap(
                    { x -> if (x.uppercase() === "DIMINISHED") StackingFormula.Diminished else StackingFormula.Flat },
                    { x -> x.name.uppercase() }
                ).fieldOf("formula") { it.formula },
                ::AttributeOverrideConfig,
            )
        }
    }
}

