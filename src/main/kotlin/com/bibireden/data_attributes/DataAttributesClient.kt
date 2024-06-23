package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.AttributeConfigManager
import com.bibireden.data_attributes.config.DataAttributesConfigScreenV2
import com.bibireden.data_attributes.networking.Channels
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import io.wispforest.owo.config.ui.ConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import java.util.concurrent.CompletableFuture

@Environment(EnvType.CLIENT)
class DataAttributesClient : ClientModInitializer {
    companion object {
        fun onPacketReceived(client: MinecraftClient, buf: PacketByteBuf) {
            buf.retain()
            client.execute {
                DataAttributes.CLIENT_MANAGER.readPacket(AttributeConfigManager.Packet.ENDEC.decodeFully(ByteBufDeserializer::of, buf))
                DataAttributes.CLIENT_MANAGER.onDataUpdate()
                buf.release()
            }
        }
    }

    override fun onInitializeClient() {
        ConfigScreen.registerProvider(DataAttributes.MOD_ID) { DataAttributesConfigScreenV2(DataAttributes.OVERRIDES_CONFIG, DataAttributes.FUNCTIONS_CONFIG, DataAttributes.ENTITY_TYPES_CONFIG, it) }

        ClientLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { client, _, buf, _ ->
            onPacketReceived(client, buf)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }

        ClientPlayNetworking.registerGlobalReceiver(Channels.RELOAD) { client, _, buf, sender ->
            onPacketReceived(client, buf)
        }
    }
}