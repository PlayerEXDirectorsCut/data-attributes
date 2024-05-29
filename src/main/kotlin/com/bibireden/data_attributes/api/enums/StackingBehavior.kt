package com.bibireden.data_attributes.api.enums

/** Refers to the kind of stacking behavior the `EntityAttribute` will utilize. */
enum class StackingBehavior {
    Add,
    Multiply;

    val uppercased = this.name.uppercase()

    companion object {
        fun of(value: String): StackingBehavior = when (value.uppercase()) {
            "MULTIPLY" -> Multiply
            else -> Add
        }
    }
}