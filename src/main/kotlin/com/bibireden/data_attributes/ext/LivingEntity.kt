package com.bibireden.data_attributes.ext

import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import net.minecraft.entity.LivingEntity

/** Refreshes the attributes of this [LivingEntity].*/
fun LivingEntity.refreshAttributes() {
    (attributes as MutableAttributeContainer).`data_attributes$refresh`()
}