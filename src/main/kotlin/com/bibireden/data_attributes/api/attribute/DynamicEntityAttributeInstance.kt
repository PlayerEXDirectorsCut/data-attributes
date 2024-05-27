package com.bibireden.data_attributes.api.attribute

import java.util.UUID

/**
 * Provides access to an entity attribute instance.
 * @author CleverNucleus
 */
interface DynamicEntityAttributeInstance {
    /**
     * Changes the value of the input modifier (if it exists) and updates the instance and all children.
     * @param uuid The uuid of the modifier.
     * @param value The value to change the modifier to.
     */
    fun updateModifier(uuid: UUID, value: Double): Unit
}