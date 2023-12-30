package com.github.clevernucleus.dataattributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.clevernucleus.dataattributes.api.item.EquipmentHelper;

import net.minecraft.item.Equipment;
import net.minecraft.sound.SoundEvent;

@Mixin(Equipment.class)
abstract class EquipmentMixin implements EquipmentHelper {

    // Overrides the getEquipSound method from EquipmentHelper
    @Override
    public SoundEvent getEquipSound(final Equipment equipment) {
        // Calls the original getEquipSound method from Equipment
        return ((Equipment) (Object) this).getEquipSound();
    }

    // Injects code at the beginning of the getEquipSound method
    @Inject(method = "getEquipSound", at = @At("HEAD"), cancellable = true, remap = false)
    private void data_getEquipSound(CallbackInfoReturnable<SoundEvent> ci) {
        // Casts 'this' to Equipment
        Equipment equipment = (Equipment) (Object) this;
        // Sets the return value for getEquipSound
        ci.setReturnValue(equipment.getEquipSound());
    }
}
