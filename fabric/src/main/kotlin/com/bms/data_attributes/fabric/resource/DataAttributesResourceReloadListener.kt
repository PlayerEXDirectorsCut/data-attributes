package com.bms.data_attributes.fabric.resource

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.Cache
import com.bms.data_attributes.resource.AttributesResourceHandler
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A listener meant to compile data-packs based on load order for preference
 * and then be applied to the manager.
 */
class DataAttributesResourceReloadListener : SimpleResourceReloadListener<Cache> {
    companion object {
        const val DIRECTORY = DataAttributes.MOD_ID
    }

    override fun getFabricId(): ResourceLocation = DataAttributes.id(DIRECTORY)

    override fun load(manager: ResourceManager, profiler: ProfilerFiller, executor: Executor): CompletableFuture<Cache> {
        return CompletableFuture.supplyAsync { AttributesResourceHandler.loadIntoCache(manager) }
    }

    override fun apply(cache: Cache, manager: ResourceManager, profiler: ProfilerFiller, executor: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync { AttributesResourceHandler.updateManager(cache) }
    }
}