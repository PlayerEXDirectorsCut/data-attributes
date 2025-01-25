package com.bms.data_attributes.mixin.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import com.bms.data_attributes.api.item.ItemHelper;

@Mixin(Item.class)
abstract class ItemMixin implements ItemHelper {
    @Override
    public Holder<SoundEvent> getEquipSound(ItemStack itemStack) {
        return !(this instanceof Equipable) ? SoundEvents.ARMOR_EQUIP_GENERIC : ((Equipable) this).getEquipSound();
    }
}
