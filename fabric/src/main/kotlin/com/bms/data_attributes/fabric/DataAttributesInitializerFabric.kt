package com.bms.data_attributes.fabric

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.DataAttributes.Companion.MANAGER
import com.bms.data_attributes.config.impl.AttributeConfigManager
import com.bms.data_attributes.ext.refreshAttributes
import com.bms.data_attributes.fabric.resource.DataAttributesResourceReloadListener
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.PlayerLookup
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.server.MinecraftServer
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.LivingEntity


/**
 * This class is the entrypoint for the mod on the Fabric platform.
 */
class DataAttributesInitializerFabric : ModInitializer {
    private val reloadListener = DataAttributesResourceReloadListener()

    private fun reload(server: MinecraftServer) {
        DataAttributes.reload()
        PlayerLookup.all(server).forEach { player -> ServerPlayNetworking.send(player, MANAGER.toPacket()) }
    }

    override fun onInitialize() {
        DataAttributes.init()

        PayloadTypeRegistry.playS2C().register(AttributeConfigManager.Packet.PACKET_ID, AttributeConfigManager.Packet.PACKET_CODEC)

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(reloadListener)

        ServerLifecycleEvents.SERVER_STARTED.register(::reload)
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, _, _ -> reload(server) }

        ServerPlayConnectionEvents.JOIN.register { _, sender, _ ->
            sender.sendPacket(MANAGER.toPacket())
        }

        ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register { _, current, _, _ ->
            if (current is LivingEntity) current.refreshAttributes()
        }
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ -> player.refreshAttributes() }
    }
}
