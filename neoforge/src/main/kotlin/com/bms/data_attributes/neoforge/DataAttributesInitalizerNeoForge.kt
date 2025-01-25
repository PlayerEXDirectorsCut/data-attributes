package com.bms.data_attributes.neoforge

import com.bms.data_attributes.DataAttributes
import net.neoforged.fml.common.Mod

/** Main class for the mod on the Forge platform. */
@Mod(DataAttributes.MOD_ID)
class DataAttributesInitializerNeoForge {
    init {
        DataAttributes.init()
    }
}
