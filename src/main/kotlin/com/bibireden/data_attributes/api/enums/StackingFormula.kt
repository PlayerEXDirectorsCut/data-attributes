package com.bibireden.data_attributes.api.enums

/** Refers to the kind of equation the attribute will use when getting stacked. */
enum class StackingFormula {
    Flat,
    Diminished;
    
    val uppercased = this.name.uppercase()

    companion object {
        fun of(value: String): StackingFormula = when (value.uppercase()) {
            "DIMINISHED" -> Diminished
            else -> Flat
        }
    }
}