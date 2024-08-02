package com.bibireden.data_attributes.fabric

import com.bibireden.data_attributes.DataAttributes
import net.fabricmc.api.ModInitializer

class DataAttributesFabric : ModInitializer {
    override fun onInitialize() {
        DataAttributes.init()
    }
}
