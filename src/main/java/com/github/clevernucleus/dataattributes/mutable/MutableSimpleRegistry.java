package com.github.clevernucleus.dataattributes.mutable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface MutableSimpleRegistry<T> {

    // Default method to remove cached identifiers from a registry
    default void removeCachedIds(Registry<T> registry) {}

    // Default method to cache an identifier
    default void cacheId(Identifier id) {}
}
