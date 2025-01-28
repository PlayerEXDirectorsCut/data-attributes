package com.bms.data_attributes.mixin;

import java.util.Collection;

import com.bms.data_attributes.api.DataAttributesAPI;
import com.bms.data_attributes.config.impl.AttributeConfigManager;
import com.bms.data_attributes.networking.NetworkingChannels;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ReloadCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.DataAttributes;

/** Hooks onto the reload command on the server to update the manager and post changes to the client. */
@Mixin(ReloadCommand.class)
abstract class ReloadCommandMixin {
    @Inject(method = "reloadPacks", at = @At("TAIL"))
    private static void data_attributes$reloadPacks(Collection<String> selectedIds, CommandSourceStack source, CallbackInfo ci) {
        DataAttributes.reloadConfigs();

        AttributeConfigManager manager = DataAttributesAPI.getServerManager();

        manager.update();
        manager.nextUpdateFlag();

        NetworkingChannels.RELOAD.serverHandle(source.getServer()).send(manager.toPacket());

        DataAttributes.LOGGER.info(
            "Updated manager with {} override(s), {} function(s) and {} entity types :: update flag [#{}]",
            DataAttributes.OVERRIDES_CONFIG.getEntries().size(),
            DataAttributes.FUNCTIONS_CONFIG.getEntries().getData().size(),
            DataAttributes.ENTITY_TYPES_CONFIG.getEntries().size(),
            manager.getUpdateFlag()
        );
    }
}