package com.bms.data_attributes.fabric

import com.bms.data_attributes.DataAttributesClient
import com.bms.data_attributes.config.impl.AttributeConfigManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

@Suppress("unused")
class DataAttributesInitializerClientFabric : ClientModInitializer {
    override fun onInitializeClient() {
        DataAttributesClient.init()

        PayloadTypeRegistry.playC2S().register(AttributeConfigManager.Packet.PACKET_ID, AttributeConfigManager.Packet.PACKET_CODEC)

        ClientPlayNetworking.registerGlobalReceiver(AttributeConfigManager.Packet.PACKET_ID) { packet, _ ->
            DataAttributesClient.onPacketReceived(packet)
        }
    }
}