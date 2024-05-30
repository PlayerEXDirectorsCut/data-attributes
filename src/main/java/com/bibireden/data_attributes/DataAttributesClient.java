package com.bibireden.data_attributes;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;

public class DataAttributesClient implements ClientModInitializer {
    // Handles the received login query packet
    private static CompletableFuture<PacketByteBuf> loginQueryReceived(MinecraftClient client,
            ClientLoginNetworkHandler handler, PacketByteBuf buf,
            Consumer<GenericFutureListener<? extends Future<? super Void>>> listenerAdder) {
        onPacketReceived(client, buf);
        return CompletableFuture.completedFuture(PacketByteBufs.empty());
    }

    // Handles the received update packet
    private static void updateReceived(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf,
            PacketSender responseSender) {
        onPacketReceived(client, buf);
    }

    // Common method for handling received packets
    private static void onPacketReceived(MinecraftClient client, PacketByteBuf buf) {
        NbtCompound tag = buf.readNbt();

        // Execute on the main thread to interact with the game state
        client.execute(() -> {
            if (tag != null) {
                // Update the local AttributeManager with data from the server
                DataAttributes.MANAGER.fromNbt(tag);
                // Apply the changes to the client's game state
                DataAttributes.MANAGER.apply();
            }
        });
    }

    // Initialization method called when the client starts
    @Override
    public void onInitializeClient() {
        // Register packet handlers for login query and update packets
        ClientLoginNetworking.registerGlobalReceiver(DataAttributes.HANDSHAKE,
                DataAttributesClient::loginQueryReceived);
        ClientPlayNetworking.registerGlobalReceiver(DataAttributes.RELOAD, DataAttributesClient::updateReceived);
    }
}
