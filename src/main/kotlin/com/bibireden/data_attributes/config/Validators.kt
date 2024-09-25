package com.bibireden.data_attributes.config

import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object Validators {
    /** Checks if the [String] is a valid number via. Regex. */
    fun isNumeric(str: String) = str.matches("^[0-9.-]*\$".toRegex())
}