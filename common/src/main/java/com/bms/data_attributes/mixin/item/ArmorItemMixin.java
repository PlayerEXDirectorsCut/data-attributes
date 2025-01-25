package com.bms.data_attributes.mixin.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorItem.class)
abstract class ArmorItemMixin extends ItemMixin {
    @Override
    public Integer getDefense(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getDefense();
    }

    @Override
    public Float getToughness(final ItemStack itemStack) {
        return ((ArmorItem) (Object) this).getToughness();
    }
}