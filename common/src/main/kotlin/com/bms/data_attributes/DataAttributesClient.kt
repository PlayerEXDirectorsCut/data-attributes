package com.bms.data_attributes

import com.bms.data_attributes.config.AttributeConfigManager
import com.bms.data_attributes.config.ConfigUIState
import com.bms.data_attributes.ui.DataAttributesConfigScreen
import io.wispforest.owo.config.ui.ConfigScreenProviders
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object DataAttributesClient {
    @JvmField
    val MANAGER = AttributeConfigManager()

    @JvmField
    val UI_STATE = ConfigUIState()

    /** Whenever the client receives a sync packet from the server to update the world-state via. configuration. */
    private fun onPacketReceived(packet: AttributeConfigManager.Packet) {
        MANAGER.readPacket(packet)
        MANAGER.onDataUpdate()
    }

    fun init() {
        PayloadTypeRegistry.playC2S().register(AttributeConfigManager.Packet.PACKET_ID, AttributeConfigManager.Packet.PACKET_CODEC)

        ConfigScreenProviders.register(DataAttributes.MOD_ID) { DataAttributesConfigScreen(DataAttributes.OVERRIDES_CONFIG, DataAttributes.FUNCTIONS_CONFIG, DataAttributes.ENTITY_TYPES_CONFIG, it) }

        ClientPlayNetworking.registerGlobalReceiver(AttributeConfigManager.Packet.PACKET_ID) { packet, _ ->
            onPacketReceived(packet)
        }
    }
}