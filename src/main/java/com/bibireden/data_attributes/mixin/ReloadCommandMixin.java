package com.bibireden.data_attributes.mixin;

import java.util.Collection;

import com.bibireden.data_attributes.data.AttributeResourceManager;
import com.bibireden.data_attributes.networking.Channels;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
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

/**
 * Intended to inject to the `tryReloadDataPacks` method to send a reload packet based on the server's manager data
 * for the client(s) to obtain.
 */
@Mixin(ReloadCommand.class)
abstract class ReloadCommandMixin {
	@Inject(method = "tryReloadDataPacks", at = @At("TAIL"))
	private static void data_tryReloadDataPacks(Collection<String> dataPacks, ServerCommandSource source, CallbackInfo ci) {
		DataAttributes.SERVER_MANAGER.nextUpdateFlag();
		PacketByteBuf buf = AttributeResourceManager.ENDEC.encodeFully(() -> ByteBufSerializer.of(PacketByteBufs.create()), DataAttributes.SERVER_MANAGER);
		PlayerLookup.all(source.getServer()).forEach(player -> ServerPlayNetworking.send(player, Channels.RELOAD, buf));
	}
}
