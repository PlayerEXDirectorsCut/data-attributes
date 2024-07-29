package com.bibireden.data_attributes.api.attribute

import io.wispforest.endec.Endec
import net.minecraft.entity.attribute.EntityAttributeInstance
import kotlin.math.abs

// CN: ((1.0 - v2) * (1.0 - m).pow((v - v2) / m)) - ((1.0 - k2) * (1.0 - m).pow((k - k2) / m))

enum class StackingFormula(val function: (k: Double, k2: Double, v: Double, v2: Double, instance: EntityAttributeInstance) -> Double) {
    Flat({ k, _, v, _, _ -> k - v }),
    Diminished({ k, k2, v, v2, instance ->
        val base = instance.baseValue
        val smoothness = (instance.attribute as IEntityAttribute).`data_attributes$smoothness`()
        val result = base + (((k - base) - v) * smoothness)
        result
    });

    companion object {
        val ENDEC: Endec<StackingFormula> = Endec.STRING.xmap(StackingFormula::of) { it.name }

        fun of(id: String) = if (id.equals("diminished", ignoreCase = true)) Diminished else Flat
    }

    val translationKey: String
        get() = "text.data_attributes.stackingFormula.${this.name.lowercase()}"

    fun max(current: Double, input: Double): Double = kotlin.math.max(current, abs(input))

    fun stack(current: Double, input: Double): Double = current + abs(input)

    fun result(k: Double, k2: Double, v: Double, v2: Double, instance: EntityAttributeInstance): Double = this.function(k, k2, v, v2, instance)
}


// the solution?
//val min = attribute.`data_attributes$min`()
//val max = attribute.`data_attributes$max`()
//
//val k = k / 100
//val k2 = k2 / 100
//val v = v / 100
//val v2 = v2 / 100
//
//val s = MathHelper.clamp(attribute.`data_attributes$smoothness`(), 0.01, 1.0)
//
//val result1 = (1.0 - v2)
//val result2 = (1.0 - s).pow((v - v2) / s)
//val result3 = result1 * result2
//
//val result4 = (1.0 - k2)
//val result5 = (1.0 - s).pow((k - k2) / s)
//val result6 = (result4 * result5)
//
//((((result3-result6)) * (max-min)) - min).round(2)