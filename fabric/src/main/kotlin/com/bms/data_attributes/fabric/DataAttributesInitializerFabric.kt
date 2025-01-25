package com.bms.data_attributes.fabric

import com.bms.data_attributes.DataAttributes
import net.fabricmc.api.ModInitializer


/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
class DataAttributesInitializerFabric : ModInitializer {
    override fun onInitialize() {
        DataAttributes.init()
    }
}
