package com.bms.data_attributes.ext

import com.bms.data_attributes.mutable.MutableAttributeMap
import net.minecraft.world.entity.LivingEntity

/** Refreshes the attributes of this [LivingEntity].*/
fun LivingEntity.refreshAttributes() {
    (attributes as MutableAttributeMap).`data_attributes$refresh`()
}