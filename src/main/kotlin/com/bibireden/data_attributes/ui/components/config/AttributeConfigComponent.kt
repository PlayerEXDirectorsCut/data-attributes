package com.bibireden.data_attributes.ui.components.config

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface AttributeConfigComponent<T> {
    val identifier: Identifier

    val registry: Registry<T>
    val registryEntry: T? get() = registry[identifier]

    /** If this [Identifier] currently exists in the backing field. */
    val isDefault: Boolean

    /** If the given entry is currently registered. */
    val isRegistered: Boolean
        get() = registryEntry != null

    fun update()
}