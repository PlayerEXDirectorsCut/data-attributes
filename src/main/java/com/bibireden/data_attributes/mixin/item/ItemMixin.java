package com.bibireden.data_attributes.mixin.item;

import com.bibireden.data_attributes.DataAttributes;
import org.spongepowered.asm.mixin.Mixin;

import com.bibireden.data_attributes.api.item.ItemHelper;

import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

@Mixin(Item.class)
abstract class ItemMixin implements ItemHelper {

    @Override
    public SoundEvent getEquipSound(final ItemStack itemStack) {
        return !(this instanceof Equipment) ? SoundEvents.ITEM_ARMOR_EQUIP_GENERIC : ((Equipment) this).getEquipSound();
    }
}
