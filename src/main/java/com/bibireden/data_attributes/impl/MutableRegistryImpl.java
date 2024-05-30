package com.bibireden.data_attributes.impl;

import com.bibireden.data_attributes.mutable.MutableSimpleRegistry;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;

public final class MutableRegistryImpl {

	@SuppressWarnings("unchecked")
	public static <T extends EntityAttribute> T register(Registry<T> registry, Identifier id, T value) {
		((MutableSimpleRegistry<T>) registry).cacheId(id);
		return Registry.register(registry, id, value);
	}

	@SuppressWarnings("unchecked")
	public static <T extends EntityAttribute> void unregister(Registry<T> registry) {
		((MutableSimpleRegistry<T>) registry).removeCachedIds(registry);
	}
}
