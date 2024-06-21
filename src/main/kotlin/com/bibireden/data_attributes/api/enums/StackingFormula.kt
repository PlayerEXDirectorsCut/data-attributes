//package com.bibireden.data_attributes.api.enums
//
//import io.wispforest.endec.Endec
//import net.minecraft.entity.attribute.EntityAttribute
//
///**
// * Determines how the calculations are made when computing [EntityAttribute] values.
//// ! DO NOT USE YET
// */
//enum class StackingFormula {
//    Flat,
//    Diminished;
//
//    companion object {
//        @JvmField
//        val ENDEC = Endec.STRING.xmap(StackingFormula::of, { it.name.uppercase() })
//
//        fun of(id: String): StackingFormula = if (id.lowercase() == "diminished") Diminished else Flat
//    }
//}