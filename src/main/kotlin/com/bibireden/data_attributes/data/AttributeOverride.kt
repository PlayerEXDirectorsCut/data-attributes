package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.attribute.StackingFormula
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute

data class AttributeOverride(val fallback: Double, val min: Double, val max: Double, val smoothness: Double, val formula: StackingFormula, val translationKey: String) {
    /** Creates a **clamped** `EntityAttribute` based on the fields of this class. */
    fun create(): EntityAttribute = ClampedEntityAttribute(this.translationKey, this.fallback, this.min, this.max)

    /** Calls an override of an `MutableEntityAttribute`. */
    fun override(mutableEntityAttribute: MutableEntityAttribute) {
        mutableEntityAttribute.`data_attributes$override`(this)
    }

    companion object {
        @JvmField
        val ENDEC: Endec<AttributeOverride> = StructEndecBuilder.of(
            Endec.DOUBLE.optionalFieldOf("fallback", { it.fallback }, 0.0),
            Endec.DOUBLE.fieldOf("min") { it.min },
            Endec.DOUBLE.fieldOf("max") { it.max },
            Endec.DOUBLE.fieldOf("smoothness") { it.smoothness },
            Endec.STRING.xmap(StackingFormula::of) { x -> x.name.uppercase() }.fieldOf("formula") { it.formula },
            Endec.STRING.fieldOf("translationKey") { it.translationKey },
            ::AttributeOverride,
        )
    }
}