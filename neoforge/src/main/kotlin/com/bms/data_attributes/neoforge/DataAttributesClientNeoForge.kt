package com.bms.data_attributes.neoforge

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.DataAttributesClient
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod

@Mod(DataAttributes.MOD_ID, dist = [Dist.CLIENT])
class DataAttributesClientNeoForge {
    init {
        DataAttributesClient.init()
    }
}
