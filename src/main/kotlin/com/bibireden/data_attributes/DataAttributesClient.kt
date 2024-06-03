package com.bibireden.data_attributes

import com.bibireden.data_attributes.data.AttributeResourceManager
import com.bibireden.data_attributes.networking.Channels
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import java.util.concurrent.CompletableFuture

class DataAttributesClient : ClientModInitializer {
    companion object {
        fun onPacketReceived(client: MinecraftClient, buf: PacketByteBuf) {
            client.execute {
                DataAttributes.CLIENT_MANAGER = AttributeResourceManager.ENDEC.decodeFully(ByteBufDeserializer::of, buf)
                DataAttributes.CLIENT_MANAGER.onDataUpdate()
            }
        }
    }

    override fun onInitializeClient() {
        ClientLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { client, _, buf, _ ->
            onPacketReceived(client, buf)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }

        ClientPlayNetworking.registerGlobalReceiver(Channels.RELOAD) { client, _, buf, sender ->
            onPacketReceived(client, buf)
        }
    }
}