package com.bibireden.data_attributes.api.attribute;

import net.minecraft.util.Identifier;

import java.util.UUID;

/**
 * 
 * Access to an entity attribute instance.
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 *
 */
public interface IEntityAttributeInstance {
	
	/**
	 * Changes the value of the input modifier (if it exists) and updates the instance and all children.
	 * @param id The id of the modifier.
	 * @param value The value to change the modifier to.
	 */
	void updateModifier(final Identifier id, final double value);
}
