package com.bibireden.data_attributes.api.attribute

import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

/**
 * Supplier classes to provide dynamic attribute references.
 */
class EntityAttributeSupplier(val id: Identifier) : Supplier<Optional<EntityAttribute>> {
    override fun get() = Optional.ofNullable(Registries.ATTRIBUTE[this.id])

    fun getOrNull() = Optional.ofNullable(Registries.ATTRIBUTE[this.id]).getOrNull()
}