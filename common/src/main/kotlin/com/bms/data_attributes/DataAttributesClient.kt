package com.bms.data_attributes

import com.bms.data_attributes.config.AttributeConfigManager
import com.bms.data_attributes.config.ConfigUIState
import com.bms.data_attributes.config.impl.AttributeConfigManager
import com.bms.data_attributes.ui.DataAttributesConfigScreen
import io.wispforest.owo.config.ui.ConfigScreenProviders

object DataAttributesClient {
    @JvmField
    val MANAGER = AttributeConfigManager()

    @JvmField
    val UI_STATE = ConfigUIState()

    /** Whenever the client receives a sync packet from the server to update the world-state via. configuration. */
    fun onPacketReceived(packet: AttributeConfigManager.Packet) {
        MANAGER.readPacket(packet)
        MANAGER.onDataUpdate()
    }

    fun init() {
        ConfigScreenProviders.register(DataAttributes.MOD_ID) { DataAttributesConfigScreen(DataAttributes.OVERRIDES_CONFIG, DataAttributes.FUNCTIONS_CONFIG, DataAttributes.ENTITY_TYPES_CONFIG, it) }
    }
}