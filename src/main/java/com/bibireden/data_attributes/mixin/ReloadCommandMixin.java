package com.bibireden.data_attributes.mixin;

import java.util.Collection;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.data_attributes.config.AttributeConfigManager;
import com.bibireden.data_attributes.networking.NetworkingChannels;
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

/** Hooks onto the reload command on the server to update the manager and post changes to the client. */
@Mixin(ReloadCommand.class)
abstract class ReloadCommandMixin {
	@Inject(method = "tryReloadDataPacks", at = @At("TAIL"))
	private static void data_attributes$tryReloadDataPacks(Collection<String> dataPacks, ServerCommandSource source, CallbackInfo ci) {
		DataAttributes.reloadConfigs();

		AttributeConfigManager manager = DataAttributesAPI.getServerManager();
		
		manager.update();
		manager.nextUpdateFlag();

		PlayerLookup.all(source.getServer()).forEach(player -> ServerPlayNetworking.send(player, manager.toPacket()));

		DataAttributes.LOGGER.info(
			"Updated manager with {} override(s), {} function(s) and {} entity types :: update flag [#{}]",
			DataAttributes.OVERRIDES_CONFIG.getOverrides().size(),
			DataAttributes.FUNCTIONS_CONFIG.getFunctions().getData().size(),
			DataAttributes.ENTITY_TYPES_CONFIG.getEntity_types().size(),
			manager.getUpdateFlag()
		);
	}
}
