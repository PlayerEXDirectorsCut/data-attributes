package com.bms.data_attributes.api.attribute

import io.wispforest.endec.Endec
import kotlin.math.abs

enum class AttributeFormat(val function: (min: Double, max: Double, value: Double) -> String) {
    Percentage ({min, max, value ->
        var minimum = 0.0
        var maximum: Double

        val isNegative = value < 0.0
        var final: Double

        if (isNegative) {
            if (max < 0)
            {
                minimum = max
            }


            maximum = min


            final = (value + minimum) / (maximum + minimum)
        } else {
            if (min > 0)
            {
                minimum = min
            }

            maximum = max

            final = (value - minimum)/(maximum-minimum)
        }

        "%.2f".format(final * 100) + "%"
    }),
    Whole({_, _, value -> "%.2f".format(value) } );

    companion object {
        val ENDEC: Endec<AttributeFormat> = Endec.STRING.xmap(AttributeFormat::of) { it.name }

        fun of(id: String) = if (id.equals("percentage", ignoreCase = true)) Percentage else Whole
    }
}