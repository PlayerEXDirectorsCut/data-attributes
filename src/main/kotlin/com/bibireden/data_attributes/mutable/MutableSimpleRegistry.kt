package com.bibireden.data_attributes.mutable

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface MutableSimpleRegistry<T : Any> {
    /** Remove cached identifiers from a registry. */
    fun removeCachedIds(registry: Registry<T>)
    /** Caches an identifier to the registry. */
    fun cacheId(id: Identifier)
}