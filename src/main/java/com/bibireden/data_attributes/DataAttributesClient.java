package com.bibireden.data_attributes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.bibireden.data_attributes.data.AttributeResourceManager;
import com.bibireden.data_attributes.endec.nbt.NbtDeserializer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.wispforest.endec.format.bytebuf.ByteBufDeserializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class DataAttributesClient implements ClientModInitializer {
    // Handles the received login query packet
    private static CompletableFuture<PacketByteBuf> loginQueryReceived(MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        onPacketReceived(client, buf);
        return CompletableFuture.completedFuture(PacketByteBufs.empty());
    }

    // Handles the received update packet
    private static void updateReceived(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        onPacketReceived(client, buf);
    }

    // Common method for handling received packets
    private static void onPacketReceived(MinecraftClient client, PacketByteBuf buf) {
        client.execute(() -> {
            DataAttributes.CLIENT_MANAGER = AttributeResourceManager.ENDEC.decodeFully(ByteBufDeserializer::of, buf);
            DataAttributes.CLIENT_MANAGER.onDataUpdate();
        });
    }

    // Initialization method called when the client starts
    @Override
    public void onInitializeClient() {
        // Register packet handlers for login query and update packets
        ClientLoginNetworking.registerGlobalReceiver(DataAttributes.HANDSHAKE, DataAttributesClient::loginQueryReceived);
        ClientPlayNetworking.registerGlobalReceiver(DataAttributes.RELOAD, DataAttributesClient::updateReceived);
    }
}
