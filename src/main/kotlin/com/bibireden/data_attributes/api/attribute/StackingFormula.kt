package com.bibireden.data_attributes.api.attribute

import io.wispforest.endec.Endec
import kotlin.math.abs
import kotlin.math.pow

// CN: ((1.0 - v2) * (1.0 - m).pow((v - v2) / m)) - ((1.0 - k2) * (1.0 - m).pow((k - k2) / m))

enum class StackingFormula(val function: (k: Double, k2: Double, v: Double, v2: Double, attribute: IEntityAttribute) -> Double) {
    Flat({ k, _, v, _, _ -> k - v }),
    Diminished({ k, k2, v, v2, attribute ->
        val min = attribute.`data_attributes$min`()
        val max = attribute.`data_attributes$max`()

        val k = k / 100
        val k2 = k2 / 100
        val v = v / 100
        val v2 = v2 / 100

        val s = attribute.`data_attributes$smoothness`()

        val result1 = (1.0 - v2)
        val result2 = (1.0 - s).pow((v - v2) / s)
        val result3 = result1 * result2

        val result4 = (1.0 - k2)
        val result5 = (1.0 - s).pow((k - k2) / s)
        val result6 = (result4 * result5)

        (((result3-result6)) * (max-min)) - min
    });

    companion object {
        val ENDEC: Endec<StackingFormula> = Endec.STRING.xmap(StackingFormula::of) { it.name }

        fun of(id: String) = if (id.equals("diminished", ignoreCase = true)) Diminished else Flat
    }

    fun max(current: Double, input: Double): Double = kotlin.math.max(current, abs(input))

    fun stack(current: Double, input: Double): Double = current + abs(input)

    fun result(k: Double, k2: Double, v: Double, v2: Double, attribute: IEntityAttribute): Double = this.function(k, k2, v, v2, attribute)
}