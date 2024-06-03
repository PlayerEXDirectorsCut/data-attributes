package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute

data class AttributeOverride(val midpoint: Double, val min: Double, val max: Double, val smoothness: Double, val formula: StackingFormula, val translationKey: String) {
    /** Creates a **clamped** `EntityAttribute` based on the fields of this class. */
    fun create(): EntityAttribute = ClampedEntityAttribute(this.translationKey, this.midpoint, this.min, this.max)

    /** Calls an override of an `MutableEntityAttribute`. */
    fun override(mutableEntityAttribute: MutableEntityAttribute) {
        mutableEntityAttribute.override(this)
    }

    companion object {
        val endec = StructEndecBuilder.of(
            Endec.DOUBLE.optionalFieldOf("midpoint", { it.midpoint }, 0.0),
            Endec.DOUBLE.optionalFieldOf("min", { it.min }, 0.0),
            Endec.DOUBLE.optionalFieldOf("max", { it.max }, Double.MAX_VALUE),
            Endec.DOUBLE.fieldOf("smoothness", { it.smoothness }),
            Endec.STRING.xmap(StackingFormula::of) { x -> x.name.uppercase() }.fieldOf("formula") { it.formula },
            Endec.STRING.fieldOf("translationKey", { it.translationKey }),
            ::AttributeOverride,
        )
    }
}