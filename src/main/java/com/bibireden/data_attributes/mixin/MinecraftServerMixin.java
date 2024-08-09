package com.bibireden.data_attributes.mixin;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.data_attributes.config.AttributeConfigManager;
import com.bibireden.data_attributes.networking.NetworkingChannels;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.DataAttributes;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hooks onto the reload command on the server to update the manager and post changes to the client. */
@Mixin(MinecraftServer.class)
abstract class MinecraftServerMixin {
	@Inject(method = "reloadResources", at = @At("TAIL"))
	private void data_attributes$reloadResources(Collection<String> dataPacks, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
//		DataAttributes.reload((MinecraftServer) (Object) this);
	}
}
