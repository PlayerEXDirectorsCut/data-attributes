package com.bibireden.data_attributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

@Mixin(ArmorItem.class)
abstract class ArmorItemMixin extends ItemMixin {
    @Override
    public Integer getProtection(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getProtection();
    }

    @Override
    public Float getToughness(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getToughness();
    }
}