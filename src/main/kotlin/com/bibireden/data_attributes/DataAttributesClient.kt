package com.bibireden.data_attributes

import com.bibireden.data_attributes.config.DataAttributesConfigScreen
import com.bibireden.data_attributes.config.screens.EntityTypesConfigScreen
import com.bibireden.data_attributes.config.screens.FunctionsConfigScreen
import com.bibireden.data_attributes.config.screens.OverridesConfigScreen
import com.bibireden.data_attributes.data.AttributeResourceManager
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
import java.io.ObjectInputFilter.Config
import java.util.concurrent.CompletableFuture

@Environment(EnvType.CLIENT)
class DataAttributesClient : ClientModInitializer {
    companion object {
        fun onPacketReceived(client: MinecraftClient, buf: PacketByteBuf) {
            buf.retain()
            client.execute {
                DataAttributes.CLIENT_MANAGER = AttributeResourceManager.ENDEC.decodeFully(ByteBufDeserializer::of, buf)
                DataAttributes.CLIENT_MANAGER.onDataUpdate()
                buf.release()
            }
        }
    }

    override fun onInitializeClient() {
        ConfigScreen.registerProvider("${DataAttributes.MOD_ID}/overrides", ::OverridesConfigScreen)
        ConfigScreen.registerProvider("${DataAttributes.MOD_ID}/functions", ::FunctionsConfigScreen)
        ConfigScreen.registerProvider("${DataAttributes.MOD_ID}/entity_types", ::EntityTypesConfigScreen)

        ClientLoginNetworking.registerGlobalReceiver(Channels.HANDSHAKE) { client, _, buf, _ ->
            onPacketReceived(client, buf)
            CompletableFuture.completedFuture(PacketByteBufs.empty())
        }

        ClientPlayNetworking.registerGlobalReceiver(Channels.RELOAD) { client, _, buf, sender ->
            onPacketReceived(client, buf)
        }
    }
}