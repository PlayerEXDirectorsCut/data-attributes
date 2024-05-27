package com.bibireden.data_attributes.mutable

import net.minecraft.entity.attribute.DefaultAttributeContainer

interface MutableDefaultAttributeContainer {
    /** Attribute configuration is copied from a `DefaultAttributeContainer.Builder`. */
    fun copy(builder: DefaultAttributeContainer.Builder)
}