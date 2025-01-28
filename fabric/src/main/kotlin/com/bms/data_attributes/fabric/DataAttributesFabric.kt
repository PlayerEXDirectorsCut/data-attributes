package com.bms.data_attributes.fabric

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.DataAttributes.Companion.MANAGER
import com.bms.data_attributes.ext.refreshAttributes
import com.bms.data_attributes.fabric.resource.DataAttributesResourceReloadListener
import com.bms.data_attributes.networking.NetworkingChannels
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.LivingEntity


/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
class DataAttributesFabric : ModInitializer {
    private val reloadListener = DataAttributesResourceReloadListener()

    override fun onInitialize() {
        DataAttributes.init()

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(reloadListener)

        ServerLifecycleEvents.SERVER_STARTED.register(DataAttributes::reload)
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> DataAttributes.reload(server) }

        ServerPlayConnectionEvents.JOIN.register { listener, _, _ ->
            NetworkingChannels.RELOAD.serverHandle(listener.player).send(MANAGER.toPacket())
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, current, _, _ ->
            if (current is LivingEntity) current.refreshAttributes()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> player.refreshAttributes() }
    }
}
