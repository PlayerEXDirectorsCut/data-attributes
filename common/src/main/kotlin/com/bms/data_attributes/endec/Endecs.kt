package com.bms.data_attributes.endec

import io.wispforest.endec.Endec
import net.minecraft.resources.ResourceLocation

object Endecs {
    @JvmField
    val RESOURCE: Endec<ResourceLocation> = Endec.STRING.xmap(ResourceLocation::parse, ResourceLocation::toString)
}