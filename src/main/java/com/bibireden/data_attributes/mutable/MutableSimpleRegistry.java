package com.bibireden.data_attributes.mutable;

import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public interface MutableSimpleRegistry<T> {
    // Default method to remove cached identifiers from a registry
    default void data_attributes$removeCachedIds(Registry<T> registry) {}

    // Default method to cache an identifier
    default void data_attributes$cacheID(Identifier id) {}
}
