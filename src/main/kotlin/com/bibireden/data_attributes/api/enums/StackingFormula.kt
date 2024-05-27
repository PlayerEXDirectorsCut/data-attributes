package com.bibireden.data_attributes.api.enums

enum class StackingFormula {
    Flat,
    Diminished;

    companion object {
        fun of(value: String): StackingFormula = when (value.uppercase()) {
            "DIMINISHED" -> Diminished
            else -> Flat
        }
    }


    fun id() {

    }

//    fun max(): Double {
//        val value = this.clamp.apply {  }
//    }
}