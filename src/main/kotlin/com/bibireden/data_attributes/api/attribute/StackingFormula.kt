package com.bibireden.data_attributes.api.attribute

import io.wispforest.endec.Endec
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.util.math.MathHelper
import kotlin.math.abs
import kotlin.math.pow

enum class StackingFormula(val function: (k: Double, k2: Double, v: Double, v2: Double, instance: EntityAttributeInstance) -> Double) {
    Flat({ k, _, v, _, _ -> k - v }),
    Diminished({ k, k2, v, v2, instance ->
        val s = (instance.attribute as IEntityAttribute).`data_attributes$smoothness`()
        ((1.0 - v2) * (1.0 - s).pow((v - v2) / s)) - ((1.0 - k2) * (1.0 - s).pow((k - k2) / s))
    });

    companion object {
        val ENDEC: Endec<StackingFormula> = Endec.STRING.xmap(StackingFormula::of) { it.name }

        fun of(id: String) = if (id.equals("diminished", ignoreCase = true)) Diminished else Flat
    }

    val translationKey: String
        get() = "text.data_attributes.stackingFormula.${this.name.lowercase()}"

    fun max(current: Double, input: Double): Double = kotlin.math.max(current, abs(input))

    fun stack(current: Double, input: Double): Double {
        var final = input
        if (this == Diminished) {
            final = MathHelper.clamp(final, -1.0, 1.0)
        }
        return current + abs(final)
    }

    fun result(k: Double, k2: Double, v: Double, v2: Double, instance: EntityAttributeInstance): Double = this.function(k, k2, v, v2, instance)
}