package com.bms.data_attributes.api.attribute;

import net.minecraft.resources.ResourceLocation;

/**
 * 
 * Access to an entity attribute instance.
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 *
 */
public interface IAttributeInstance {
	
	/**
	 * Changes the value of the input modifier (if it exists) and updates the instance and all children.
	 * @param uuid The uuid of the modifier.
	 * @param value The value to change the modifier to.
	 */
	default void data_attributes$updateModifier(final ResourceLocation id, final double value) {}
}
