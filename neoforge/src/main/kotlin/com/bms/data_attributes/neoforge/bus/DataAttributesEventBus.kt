package com.bms.data_attributes.neoforge.bus

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.Cache
import com.bms.data_attributes.ext.refreshAttributes
import com.bms.data_attributes.networking.NetworkingChannels
import com.bms.data_attributes.resource.AttributesResourceHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.EventBusSubscriber.Bus
import net.neoforged.neoforge.event.AddReloadListenerEvent
import net.neoforged.neoforge.event.OnDatapackSyncEvent
import net.neoforged.neoforge.event.entity.EntityTeleportEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import java.util.concurrent.CompletableFuture

@Suppress("unused")
@EventBusSubscriber(modid = DataAttributes.MOD_ID, bus = Bus.GAME)
object DataAttributesEventBus {
    private var cache = Cache()

    @SubscribeEvent
    private fun onServerStarted(event: ServerStartedEvent) {
        DataAttributes.reload(event.server)
    }

    @SubscribeEvent
    private fun onPackSync(event: OnDatapackSyncEvent) {
        if (event.player is ServerPlayer) {
            NetworkingChannels.RELOAD.serverHandle(event.player).send(DataAttributes.MANAGER.toPacket())
        }
    }

    @SubscribeEvent
    private fun onPlayerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) {
        event.entity.refreshAttributes()
    }

    @SubscribeEvent
    private fun onEntityTeleported(event: EntityTeleportEvent) {
        val entity = event.entity
        if (entity is LivingEntity) entity.refreshAttributes()
    }

    @SubscribeEvent
    private fun resourceEvent(event: AddReloadListenerEvent) {
        event.addListener { barrier, manager, _, _, _, _ ->
            CompletableFuture.runAsync { cache = AttributesResourceHandler.loadIntoCache(manager) }.thenCompose(barrier::wait).thenAcceptAsync { AttributesResourceHandler.updateManager(cache) }
        }
    }
}