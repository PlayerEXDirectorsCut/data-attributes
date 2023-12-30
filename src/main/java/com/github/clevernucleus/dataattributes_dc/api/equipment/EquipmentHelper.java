package com.github.clevernucleus.dataattributes_dc.api.equipment;

import net.minecraft.item.Equipment;
import net.minecraft.sound.SoundEvent;

public interface EquipmentHelper {
    /**
     * Equipment dependent version of Equiipment#getEquipSound. Default
     * implementation
     * returns aforementioned.
     * 
     * @param itemStack
     * @return
     */
    default SoundEvent getEquipSound(final Equipment equipment) {
        return null;
    }
}
