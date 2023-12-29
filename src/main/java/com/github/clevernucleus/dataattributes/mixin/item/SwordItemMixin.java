package com.github.clevernucleus.dataattributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

@Mixin(SwordItem.class)
abstract class SwordItemMixin extends ItemMixin {

    // Overrides the getAttackDamage method from ItemMixin
    @Override
    public float getAttackDamage(final ItemStack itemStack) {
        // Calls the original getAttackDamage method from SwordItem
        return ((SwordItem) (Object) this).getAttackDamage();
    }
}
