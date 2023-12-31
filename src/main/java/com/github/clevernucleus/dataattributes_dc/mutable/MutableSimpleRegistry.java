package com.github.clevernucleus.dataattributes_dc.mutable;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public interface MutableSimpleRegistry<T> {

    // Default method to remove cached identifiers from a registry
    default void removeCachedIds(Registry<T> registry) {
    }

    // Default method to cache an identifier
    default void cacheId(Identifier id) {
    }
}
