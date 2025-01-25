@file:UseSerializers(ResourceLocationSerializer::class)

package com.bms.data_attributes.config.entry

import com.bms.data_attributes.serde.ResourceLocationSerializer
import kotlinx.serialization.UseSerializers

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.config.entities.EntityTypeEntry
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.profiling.ProfilerFiller
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * A listener meant to compile [Pack]'s based on load order for preference
 * and then be applied to the manager.
 */
@OptIn(ExperimentalSerializationApi::class)
class DefaultAttributesReloadListener : SimpleResourceReloadListener<DefaultAttributesReloadListener.Cache> {
    var data: Cache = Cache()

    @Serializable
    data class Overrides(var entries: LinkedHashMap<ResourceLocation, AttributeOverride> = LinkedHashMap())
    @Serializable
    data class Functions(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, AttributeFunction>> = LinkedHashMap())
    @Serializable
    data class EntityTypes(var entries: LinkedHashMap<ResourceLocation, LinkedHashMap<ResourceLocation, EntityTypeEntry>> = LinkedHashMap())

    @Serializable
    data class Cache(val overrides: Overrides = Overrides(), val functions: Functions = Functions(), val types: EntityTypes = EntityTypes())

    companion object {
        const val DIRECTORY = DataAttributes.MOD_ID
    }

    private fun isPathJson(id: ResourceLocation) = id.path.endsWith(".json")

    override fun getFabricId(): ResourceLocation = DataAttributes.id(DIRECTORY)

    override fun load(manager: ResourceManager, profiler: ProfilerFiller, executor: Executor): CompletableFuture<Cache> {
        return CompletableFuture.supplyAsync {
            val cache = Cache()

            readOverrides(manager, cache)
            readFunctions(manager, cache)
            readEntityTypes(manager, cache)

            // Filtering before applying the cache.
            cache.overrides.entries = LinkedHashMap(cache.overrides.entries.filter { (id, _) -> BuiltInRegistries.ATTRIBUTE.containsKey(id) })
            cache.functions.entries.forEach { (id, functions) ->
                if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                    cache.functions.entries.remove(id)
                    return@forEach
                }
                cache.functions.entries[id] = LinkedHashMap(functions.filter { (id2) -> BuiltInRegistries.ATTRIBUTE.containsKey(id2) })
            }
            cache.types.entries.forEach { (id, data) ->
                if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                    cache.types.entries.remove(id)
                    return@forEach
                }
                cache.types.entries[id] = LinkedHashMap(data.filter { (id2) -> BuiltInRegistries.ATTRIBUTE.containsKey(id2) })
            }

            cache
        }
    }

    override fun apply(cache: Cache, manager: ResourceManager, profiler: ProfilerFiller, executor: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync { DataAttributes.MANAGER.defaults = cache }
    }

    private fun readOverrides(manager: ResourceManager, cache: Cache) {
        val path = "$DIRECTORY/overrides"

        manager.listResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<Overrides>(res.open()).entries.forEach { (id, entry) ->
                    cache.overrides.entries.computeIfAbsent(id) { entry }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse overrides@$id :: {}", why.message)
            }
        }
    }

    private fun readFunctions(manager: ResourceManager, cache: Cache) {
        val path = "$DIRECTORY/functions"

        manager.listResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<Functions>(res.open()).entries.forEach { (id, entry) ->
                    val presentEntry = cache.functions.entries[id]
                    if (presentEntry == null) {
                        cache.functions.entries[id] = entry
                    }
                    else {
                        for ((secondaryId, secondaryValue) in entry) {
                            presentEntry.computeIfAbsent(secondaryId) { secondaryValue }
                        }
                        cache.functions.entries[id] = presentEntry
                    }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse functions@$id :: {}", why.message)
            }
        }
    }

    private fun readEntityTypes(manager: ResourceManager, cache: Cache) {
        val path = "$DIRECTORY/entity_types"

        manager.listResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<EntityTypes>(res.open()).entries.forEach { (id, entry) ->
                    val presentEntry = cache.types.entries[id]
                    if (presentEntry == null) {
                        cache.types.entries[id] = entry
                    }
                    else {
                        for ((secondaryId, secondaryValue) in entry) {
                            presentEntry.computeIfAbsent(secondaryId) { secondaryValue }
                        }
                        cache.types.entries[id] = presentEntry
                    }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse entity-types@$id :: {}", why.message)
            }
        }
    }
}