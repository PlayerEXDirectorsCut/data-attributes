package com.bibireden.data_attributes.api.attribute

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier

/**
 * Meant to be implemented in your `Item` class.
 * @author CleverNucleus
 */
interface DynamicEntityAttributeModifiers {
    /**
     * Provides a **`mutable` attribute modifier multi-map** so items can have dynamically changing modifiers based on `nbt` data.
     */
    fun getAttributeModifiers(): Multimap<EntityAttribute, EntityAttributeModifier> {
        return HashMultimap.create()
    }
}