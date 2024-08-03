package com.bibireden.data_attributes.api.item;

import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * Helper class that exposes {@link Item#BASE_ATTACK_DAMAGE_MODIFIER_ID} and {@link Item#BASE_ATTACK_SPEED_MODIFIER_ID}.
 * 
 * @author CleverNucleus
 *
 */
public final class ItemFields extends Item {
	private ItemFields() { super(new Item.Settings()); }
	
	public static Identifier attackDamageModifierID() { return BASE_ATTACK_DAMAGE_MODIFIER_ID; }
	
	public static Identifier attackSpeedModifierID() { return BASE_ATTACK_SPEED_MODIFIER_ID; }
}
