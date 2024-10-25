package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.config.ConfigUIState
import com.bibireden.data_attributes.networking.NetworkingChannels
import com.bibireden.data_attributes.ui.DataAttributesConfigScreen
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.owo.config.ui.ConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import java.util.concurrent.CompletableFuture

class DataAttributesClient : ClientModInitializer {
    companion object {
        @JvmField
        val MANAGER = AttributeConfigManager()

        @JvmField
        val UI_STATE = ConfigUIState()

        /** Whenever the client receives a sync packet from the server to update the world-state via. configuration. */
        fun onPacketReceived(client: MinecraftClient, buf: PacketByteBuf) {
            buf.retain()
            client.execute {
                MANAGER.readPacket(AttributeConfigManager.Packet.ENDEC.decodeFully(ByteBufDeserializer::of, buf))
                MANAGER.onDataUpdate()
                buf.release()
            }
        }
    }

    override fun onInitializeClient() {
        ConfigScreen.registerProvider(DataAttributes.MOD_ID) { DataAttributesConfigScreen(DataAttributes.OVERRIDES_CONFIG, DataAttributes.FUNCTIONS_CONFIG, DataAttributes.ENTITY_TYPES_CONFIG, it) }

        ClientLoginNetworking.registerGlobalReceiver(NetworkingChannels.HANDSHAKE) { client, _, buf, _ ->
            onPacketReceived(client, buf)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }
        ClientPlayNetworking.registerGlobalReceiver(NetworkingChannels.RELOAD) { client, _, buf, _ ->
            onPacketReceived(client, buf)
        }
    }
}