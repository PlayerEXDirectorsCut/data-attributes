package com.bms.data_attributes.ui.components.config

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

interface AttributeConfigComponent<T> {
    val identifier: ResourceLocation

    val registry: Registry<T>
    val registryEntry: T? get() = registry[identifier]

    /** If this [ResourceLocation] currently exists in the backing field. */
    val isDefault: Boolean

    /** If the given entry is currently registered. */
    val isRegistered: Boolean
        get() = registryEntry != null

    fun update()
}