package com.bibireden.data_attributes.api.enums

enum class StackingFormula {
    Flat,
    Diminishing;

    companion object {
        fun of(value: String): StackingFormula = when (value.uppercase()) {
            "DIMINISHING" -> Diminishing
            else -> Flat
        }
    }
}