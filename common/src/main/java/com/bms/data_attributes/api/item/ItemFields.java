package com.bms.data_attributes.api.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


/**
 * Helper class that exposes {@link Item#BASE_ATTACK_DAMAGE_ID} and {@link Item#BASE_ATTACK_SPEED_ID}.
 * 
 * @author CleverNucleus
 *
 */
public final class ItemFields extends Item {
	private ItemFields() { super(new Item.Properties()); }
	
	public static ResourceLocation attackDamageModifierID() { return BASE_ATTACK_DAMAGE_ID; }
	
	public static ResourceLocation attackSpeedModifierID() { return BASE_ATTACK_SPEED_ID; }
}
