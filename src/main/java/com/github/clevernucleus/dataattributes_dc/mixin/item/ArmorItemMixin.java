package com.github.clevernucleus.dataattributes_dc.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

@Mixin(ArmorItem.class)
abstract class ArmorItemMixin extends ItemMixin {

    // Overrides the getProtection method from ItemMixin
    @Override
    public int getProtection(final ItemStack itemStack) {
        // Calls the original getProtection method from ArmorItem
        return ((ArmorItem) (Object) this).getProtection();
    }

    // Overrides the getToughness method from ItemMixin
    @Override
    public float getToughness(final ItemStack itemStack) {
        // Calls the original getToughness method from ArmorItem
        return ((ArmorItem) (Object) this).getToughness();
    }
}
