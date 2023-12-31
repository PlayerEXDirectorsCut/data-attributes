package com.github.clevernucleus.dataattributes_dc.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

@Mixin(ArmorItem.class)
abstract class ArmorItemMixin extends ItemMixin {

    @Override
    public int getProtection(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getProtection();
    }

    @Override
    public float getToughness(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getToughness();
    }
}