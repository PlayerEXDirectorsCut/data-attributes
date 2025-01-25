package com.bms.data_attributes.api.attribute;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this in your Item class.
 * 
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 *
 */
public interface IItemAttributeModifiers {
	
	/**
	 * This method provides a mutable attribute modifier multimap so that items can have dynamically changing modifiers based on nbt.
	 * @param itemStack
	 * @param slot
	 * @return
	 */
	default Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack itemStack, EquipmentSlot slot) {
		return HashMultimap.create();
	}
}
