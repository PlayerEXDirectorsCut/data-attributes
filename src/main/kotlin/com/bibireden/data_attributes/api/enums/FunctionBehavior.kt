package com.bibireden.data_attributes.api.enums

enum class FunctionBehavior {
    Add,
    Multiply;

    companion object {
        fun of(value: String): FunctionBehavior = when (value.uppercase()) {
            "MULTIPLY" -> Multiply
            else -> Add
        }
    }
}