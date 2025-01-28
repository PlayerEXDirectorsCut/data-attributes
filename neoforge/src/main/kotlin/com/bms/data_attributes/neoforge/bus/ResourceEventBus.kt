package com.bms.data_attributes.neoforge.bus

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.Cache
import com.bms.data_attributes.resource.AttributesResourceHandler
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.EventBusSubscriber.Bus
import net.neoforged.neoforge.event.AddReloadListenerEvent
import java.util.concurrent.CompletableFuture

@Suppress("unused")
@EventBusSubscriber(modid = DataAttributes.MOD_ID, bus = Bus.GAME)
class ResourceEventBus {
    private var cache = Cache()

    @SubscribeEvent
    fun resourceEvent(event: AddReloadListenerEvent) {
        event.addListener { barrier, manager, _, _, _, _ ->
            CompletableFuture.runAsync { cache = AttributesResourceHandler.loadIntoCache(manager) }.thenCompose(barrier::wait).thenAcceptAsync { AttributesResourceHandler.updateManager(cache) }
        }
    }
}