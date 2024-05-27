package com.bibireden.data_attributes.mixin.item;


import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;

@Mixin(MiningToolItem.class)
abstract class MiningToolItemMixin extends ItemMixin {

    // Overrides the getAttackDamage method from ItemMixin
    @Override
    public float getAttackDamage(final ItemStack itemStack) {
        // Calls the original getAttackDamage method from MiningToolItem
        return ((MiningToolItem) (Object) this).getAttackDamage();
    }
}