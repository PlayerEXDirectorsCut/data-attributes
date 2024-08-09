@file:UseSerializers(IdentifierSerializer::class)

package com.bibireden.data_attributes.config.entry

import com.bibireden.data_attributes.serde.IdentifierSerializer
import kotlinx.serialization.UseSerializers

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.config.functions.AttributeFunction
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bibireden.data_attributes.data.EntityTypeData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
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
    data class Overrides(val entries: LinkedHashMap<Identifier, AttributeOverride> = LinkedHashMap())
    @Serializable
    data class Functions(val entries: LinkedHashMap<Identifier, List<AttributeFunction>> = LinkedHashMap())
    @Serializable
    data class EntityTypes(val entries: LinkedHashMap<Identifier, LinkedHashMap<Identifier, Double>> = LinkedHashMap())

    @Serializable
    data class Cache(val overrides: Overrides = Overrides(), val functions: Functions = Functions(), val types: EntityTypes = EntityTypes())

    companion object {
        const val DIRECTORY = DataAttributes.MOD_ID
    }

    private fun isPathJson(id: Identifier) = id.path.endsWith(".json")

    override fun getFabricId(): Identifier = DataAttributes.id(DIRECTORY)

    override fun load(manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Cache> {
        return CompletableFuture.supplyAsync {
            val cache = Cache()

            readOverrides(manager, cache)
            readFunctions(manager, cache)
            readEntityTypes(manager, cache)

            cache
        }
    }

    override fun apply(cache: Cache, manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync { DataAttributes.MANAGER.defaults = cache }
    }

    private fun readOverrides(manager: ResourceManager, cache: Cache) {
        val path = "$DIRECTORY/overrides"

        manager.findResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<Overrides>(res.inputStream).entries.forEach { (id, entry) ->
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

        manager.findResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<Functions>(res.inputStream).entries.forEach { (id, entry) ->
                    cache.functions.entries.computeIfAbsent(id) { entry }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse functions@$id :: {}", why.message)
            }
        }
    }

    private fun readEntityTypes(manager: ResourceManager, cache: Cache) {
        val path = "$DIRECTORY/entity_types"

        manager.findResources(path, ::isPathJson).forEach { (id, res) ->
            try {
                Json.decodeFromStream<EntityTypes>(res.inputStream).entries.forEach { (id, entry) ->
                    cache.types.entries.computeIfAbsent(id) { entry }
                }
            }
            catch (why: Exception) {
                DataAttributes.LOGGER.error("Failed to parse entity-types@$id :: {}", why.message)
            }
        }
    }
}