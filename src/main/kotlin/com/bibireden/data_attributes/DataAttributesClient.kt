package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.networking.Channels
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
        private val MANAGER = AttributeConfigManager()

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

        ClientLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { client, _, buf, _ ->
            onPacketReceived(client, buf)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }
        ClientPlayNetworking.registerGlobalReceiver(Channels.RELOAD) { client, _, buf, _ ->
            onPacketReceived(client, buf)
        }
    }
}