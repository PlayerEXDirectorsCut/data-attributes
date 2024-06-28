package com.bibireden.data_attributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

@Mixin(SwordItem.class)
abstract class SwordItemMixin extends ItemMixin {
    @Override
    public Float getAttackDamage(final ItemStack itemStack) {
        return ((SwordItem) (Object) this).getAttackDamage();
    }
}
