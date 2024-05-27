package com.bibireden.data_attributes.mutable

import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttributeInstance
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.util.Identifier

interface MutableAttributeInstance {
    /** The unique identifier of the attribute instance. */
    fun id(): Identifier
    /** Performs an action on the attribute modifier. */
    fun actionModifier(func: () -> Unit, entityAttributeInstance: EntityAttributeInstance, modifier: EntityAttributeModifier, isAdded: Boolean)
    /** Applies a callback for changes to the `AttributeContainer`. */
    fun updateId(id: Identifier)
    /** Refreshes the attribute instance.*/
    fun refresh()
    /** Sets a callback for changes to the associated AttributeContainer */
    fun setContainerCallback(containerIn: AttributeContainer)
}