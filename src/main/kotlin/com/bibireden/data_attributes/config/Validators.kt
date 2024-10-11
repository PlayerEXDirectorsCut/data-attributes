package com.bibireden.data_attributes.config

import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object Validators {
    /** Checks if the [String] is a valid number via. Regex. */
    fun isNumeric(str: String) = str.matches("^[0-9.-]*\$".toRegex())

    fun isRegisteredAttributeId(str: String): Boolean {
        val id = Identifier.tryParse(str) ?: return false
        return Registries.ATTRIBUTE[id] != null
    }
}