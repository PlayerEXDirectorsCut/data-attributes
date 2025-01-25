package com.bms.data_attributes.api.attribute

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.ai.attributes.Attribute
import java.util.*
import java.util.function.Supplier
import kotlin.jvm.optionals.getOrNull

/**
 * Supplier classes to provide dynamic attribute references.
 */
class AttributeSupplier(val id: ResourceLocation) : Supplier<Optional<Attribute>> {
    override fun get() = Optional.ofNullable(BuiltInRegistries.ATTRIBUTE[this.id])

    fun getOrNull() = Optional.ofNullable(BuiltInRegistries.ATTRIBUTE[this.id]).getOrNull()
}