package com.bibireden.data_attributes.api.attribute

import io.wispforest.endec.Endec
import net.minecraft.entity.attribute.EntityAttributeInstance
import kotlin.math.abs

enum class StackingFormula(val function: (k: Double, k2: Double, v: Double, v2: Double, instance: EntityAttributeInstance) -> Double) {
    Flat({ k, _, v, _, _ -> k - v }),
    Diminished({ k, k2, v, v2, instance ->
        val base = instance.baseValue
        val smoothness = (instance.attribute as IEntityAttribute).`data_attributes$smoothness`()
        base + (((k - base) - v) * smoothness)
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