package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.networking.NetworkingChannels
import com.bibireden.data_attributes.ui.DataAttributesConfigScreen
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.owo.config.ui.ConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import java.util.concurrent.CompletableFuture

class DataAttributesClient : ClientModInitializer {
    companion object {
        @JvmField
        val MANAGER = AttributeConfigManager()

        /** Whenever the client receives a sync packet from the server to update the world-state via. configuration. */
        fun onPacketReceived(packet: AttributeConfigManager.Packet) {
            MANAGER.readPacket(packet)
            MANAGER.onDataUpdate()
        }
    }

    override fun onInitializeClient() {
        ConfigScreen.registerProvider(DataAttributes.MOD_ID) { DataAttributesConfigScreen(DataAttributes.OVERRIDES_CONFIG, DataAttributes.FUNCTIONS_CONFIG, DataAttributes.ENTITY_TYPES_CONFIG, it) }

        ClientLoginNetworking.registerGlobalReceiver(NetworkingChannels.HANDSHAKE) { client, _, buf, _ ->
            AttributeConfigManager.Packet.ENDEC.decodeFully(ByteBufDeserializer::of, buf)?.let(::onPacketReceived)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }

        ClientPlayNetworking.registerGlobalReceiver(AttributeConfigManager.Packet.PACKET_ID) { packet, _ ->
            onPacketReceived(packet)
        }
    }
}