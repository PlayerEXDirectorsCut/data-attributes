package com.bms.data_attributes.mixin.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
abstract class SwordItemMixin extends ItemMixin {
   @Override
   public Integer getAttackDamage(final ItemStack itemStack) {
       return itemStack.getDamageValue();
   }
}
