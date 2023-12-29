package com.github.clevernucleus.dataattributes.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.dataattributes.DataAttributes;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ReloadCommand;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(ReloadCommand.class)
abstract class ReloadCommandMixin {
	
    // Injection at the 'tryReloadDataPacks' method, after its normal execution
	@Inject(method = "tryReloadDataPacks", at = @At("TAIL"))
	private static void data_tryReloadDataPacks(Collection<String> dataPacks, ServerCommandSource source, CallbackInfo ci) {
        // Access the server from the command source
		MinecraftServer server = source.getServer();
		
        // Create a new PacketByteBuf for network communication
		PacketByteBuf buf = PacketByteBufs.create();
		
        // Create an NbtCompound to store data attributes information
		NbtCompound tag = new NbtCompound();
		
        // Trigger the next update flag in the data attributes manager
		DataAttributes.MANAGER.nextUpdateFlag();
		
        // Save data attributes information to the NbtCompound
		DataAttributes.MANAGER.toNbt(tag);
		
        // Write the NbtCompound to the PacketByteBuf
		buf.writeNbt(tag);
		
        // Send a custom network packet (DataAttributes.RELOAD) to all players on the server
		PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.send(player, DataAttributes.RELOAD, buf));
	}
}
