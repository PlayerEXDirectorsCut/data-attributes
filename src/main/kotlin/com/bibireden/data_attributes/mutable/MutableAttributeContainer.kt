package com.bibireden.data_attributes.mutable

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.util.Identifier

interface MutableAttributeContainer {
    // Returns a map of custom attributes associated with this container
    fun custom(): Map<Identifier, EntityAttributeInstance>

    // Returns the LivingEntity associated with this container
    fun getLivingEntity() : LivingEntity

    // Sets the LivingEntity associated with this container
    fun setLivingEntity(livingEntity: LivingEntity): Unit

    // Refreshes the attributes of the associated LivingEntity
    fun refresh(): Unit

    // Clears any tracked information (possibly related to attribute changes)
    fun clearTracked(): Unit
}