package com.bms.data_attributes.config

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object Validators {
    /** Checks if the [String] is a valid number via. Regex. */
    fun isNumeric(str: String) = str.matches("^[0-9.-]*\$".toRegex())

    fun isRegisteredAttributeId(str: String): Boolean {
        val id = ResourceLocation.tryParse(str) ?: return false
        return BuiltInRegistries.ATTRIBUTE[id] != null
    }
}