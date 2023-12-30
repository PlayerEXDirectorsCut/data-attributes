package com.github.clevernucleus.dataattributes_dc.mixin.item;

import org.spongepowered.asm.mixin.Mixin;

import com.github.clevernucleus.dataattributes_dc.api.item.ItemHelper;

import net.minecraft.item.Item;

@Mixin(Item.class)
abstract class ItemMixin implements ItemHelper {
}
