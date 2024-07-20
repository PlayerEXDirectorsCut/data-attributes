package com.bibireden.data_attributes.api.attribute

import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Supplier

/**
 * Supplier classes to provide dynamic attribute references.
 */
class EntityAttributeSupplier(val id: Identifier) : Supplier<EntityAttribute?> {
    override fun get() = Registries.ATTRIBUTE[this.id]
}