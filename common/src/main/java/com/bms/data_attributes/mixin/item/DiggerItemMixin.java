package com.bms.data_attributes.mixin.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;


@Mixin(DiggerItem.class)
abstract class DiggerItemMixin extends ItemMixin {
   @Override
   public Integer getAttackDamage(final ItemStack itemStack) {
       return itemStack.getDamageValue();
   }
}
