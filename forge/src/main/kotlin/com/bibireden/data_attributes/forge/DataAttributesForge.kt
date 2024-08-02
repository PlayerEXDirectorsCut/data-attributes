package com.bibireden.data_attributes.forge

import com.bibireden.data_attributes.DataAttributes
import net.minecraftforge.fml.common.Mod

@Mod(DataAttributes.MOD_ID)
class DataAttributesForge {
    init {
        DataAttributes.init()
    }
}
