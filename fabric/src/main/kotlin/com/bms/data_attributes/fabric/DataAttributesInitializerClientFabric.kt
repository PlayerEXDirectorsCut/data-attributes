package com.bms.data_attributes.fabric

import com.bms.data_attributes.DataAttributesClient
import net.fabricmc.api.ClientModInitializer

@Suppress("unused")
class DataAttributesInitializerClientFabric : ClientModInitializer {
    override fun onInitializeClient() {
        DataAttributesClient.init()
    }
}