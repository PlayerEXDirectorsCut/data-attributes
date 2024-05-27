package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.mutable.MutableSimpleRegistry
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object DynamicMutableRegistry {
    @Suppress("UNCHECKED_CAST")
    fun <T : EntityAttribute> register(registry: Registry<T>, identifier: Identifier, value: T): T {
        (registry as MutableSimpleRegistry<T>).cacheId(identifier)
        return Registry.register(registry, identifier, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : EntityAttribute> unregister(registry: Registry<T>) {
        (registry as MutableSimpleRegistry<T>).removeCachedIds(registry)
    }
}