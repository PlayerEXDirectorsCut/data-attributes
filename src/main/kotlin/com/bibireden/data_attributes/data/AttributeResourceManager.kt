package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.EntityInstances
import com.bibireden.data_attributes.data.merged.AttributeFunctions
import com.bibireden.data_attributes.data.merged.EntityTypes
import com.bibireden.data_attributes.impl.AttributeContainerHandler
import com.bibireden.data_attributes.impl.MutableRegistryImpl
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mojang.logging.LogUtils
import io.wispforest.endec.CodecUtils
import io.wispforest.endec.Endec
import io.wispforest.endec.format.json.JsonDeserializer
import io.wispforest.endec.impl.StructEndecBuilder
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

private typealias EntityAttributeBank = Map<Identifier, EntityAttributeData>
private typealias EntityTypesBank = Map<Identifier, EntityTypeData>

/** Meant to manage the listening and application of all data-packs related to the `attribute` registry for DataAttributes. */
class AttributeResourceManager(
    var attributes: EntityAttributeBank = mapOf(),
    var types: EntityTypesBank = mapOf(),
    val handler: AttributeContainerHandler = AttributeContainerHandler(),
    var updateFlag: Int = 0
) : SimpleResourceReloadListener<AttributeResourceManager.Data> {
    companion object {
        const val DIRECTORY = "attributes"
        const val PATH_SUFFIX_LENGTH = ".json".length

        val ID = DataAttributesAPI.id(DIRECTORY)
        val GSON = GsonBuilder().create()
        val LOGGER = LogUtils.getLogger()

        @JvmField
        val ENDEC = StructEndecBuilder.of(
            Endec.map(CodecUtils.ofCodec(Identifier.CODEC), EntityAttributeData.ENDEC).fieldOf("attributes") { it.attributes },
            Endec.map(CodecUtils.ofCodec(Identifier.CODEC), EntityTypeData.ENDEC).fieldOf("types") { it.types },
            ::AttributeResourceManager
        )

        val ENTITY_TYPE_INSTANCES = mapOf(
            EntityInstances.LIVING     to Tuple(LivingEntity::class.java, 0),
            EntityInstances.MOB        to Tuple(MobEntity::class.java, 1),
            EntityInstances.PATH_AWARE to Tuple(PathAwareEntity::class.java, 2),
            EntityInstances.HOSTILE    to Tuple(HostileEntity::class.java, 3),
            EntityInstances.PASSIVE    to Tuple(PassiveEntity::class.java, 4),
            EntityInstances.ANIMAL     to Tuple(AnimalEntity::class.java, 5)
        )

        /** Gets or registers an [EntityAttribute] based on the provided identifier key and attribute. */
        fun getOrCreate(identifier: Identifier, attribute: EntityAttribute?): EntityAttribute = Registries.ATTRIBUTE[identifier] ?: MutableRegistryImpl.register(Registries.ATTRIBUTE, identifier, attribute)

        // Passing in overrides to the data argument.
        fun loadOverrides(manager: ResourceManager, data: MutableMap<Identifier, EntityAttributeData>) {
            val location = "$DIRECTORY/overrides"
            val length = location.length + 1

            val cached = mutableMapOf<Identifier, AttributeOverride>()

            for ((resource, value) in manager.findResources(location) { it.path.endsWith(".json") }) {
                val path = resource.path

                val identifier = Identifier.of(resource.namespace, path.substring(length, path.length - PATH_SUFFIX_LENGTH))!!

                value.reader.use { reader ->
                    try {
                        val override = AttributeOverride.ENDEC.decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement::class.java))
                        if (cached.put(identifier, override) != null) LOGGER.warn("Overriding override found with duplicate: {}", identifier)
                    } catch (e: Exception) {
                        LOGGER.error("Failed to parse override data file {} from {} :: {}", identifier, resource, e);
                    }
                }
            }

            cached.forEach { (identifier, value) -> data[identifier] = EntityAttributeData(value) }
        }

        // Passing in functions to the data argument.
        fun loadFunctions(manager: ResourceManager, data: MutableMap<Identifier, EntityAttributeData>) {
            val cached = mutableMapOf<Identifier, AttributeFunctions>()
            val length = DIRECTORY.length + 1

            for ((resource, value) in manager.findResources(DIRECTORY) { it.path.endsWith("functions.json") }) {
                val path = resource.path

                val identifier = Identifier.of(resource.namespace, path.substring(length, path.length - PATH_SUFFIX_LENGTH))!!

                value.reader.use { reader ->
                    try {
                        val functions = AttributeFunctions.ENDEC.decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement::class.java))
                        if (cached.put(identifier, functions) != null) LOGGER.warn("Overriding function(s) found with duplicate: {}", identifier)
                    } catch (e: Exception) {
                        LOGGER.error("Failed to parse function data file {} from {} :: {}", identifier, resource, e);
                    }
                }
            }

            val functions = mutableMapOf<String, MutableMap<String, AttributeFunction>>()
            cached.values.forEach { it.merge(functions) }

            for ((key, value) in functions) {
                val id = Identifier(key)
                val dat = data.getOrDefault(id, EntityAttributeData())
                dat.putFunctions(formatFunctions(value))
                data[id] = dat
            }
        }

        // Passing in entity types to the data argument.
        fun loadEntityTypes(manager: ResourceManager, data: MutableMap<Identifier, EntityTypeData>) {
            val cached = mutableMapOf<Identifier, EntityTypes>()
            val length = DIRECTORY.length + 1

            for ((resource, value) in manager.findResources(DIRECTORY) { it.path.endsWith("entity_types.json") }) {
                val path = resource.path

                val identifier = Identifier.of(resource.namespace, path.substring(length, path.length - PATH_SUFFIX_LENGTH))!!

                value.reader.use { reader ->
                    try {
                        val types = EntityTypes.ENDEC.decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement::class.java))
                        if (cached.put(identifier, types) != null) LOGGER.warn("Overriding entity-types(s) found with duplicate: {}", identifier)
                    } catch (e: Exception) {
                        LOGGER.error("Failed to parse function data file {} from {} :: {}", identifier, resource, e);
                    }
                }
            }

            val types = mutableMapOf<String, MutableMap<String, Double>>()
            cached.values.forEach { it.merge(types) }

            for ((key, value) in types) {
                val id = Identifier(key)
                val dat = EntityTypeData(value.map { (k, v) -> Identifier(k) to v }.toMap().toMutableMap())
                data[id] = dat
            }
        }

//        /** Replacement of the Tuple. Uses [Pair] to achieve the same goal. */
//        fun <T> pair(entity: Class<out LivingEntity>, value: T) = Pair(entity, value)

        // todo: These get formatted to map to identifiers. Let us use the Endec system to avoid doing this soon.
        fun formatFunctions(functions: Map<String, AttributeFunction>) = functions.map { (key, value) -> Identifier(key) to value }.toMap()
    }

    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

    /** Data wrapper that holds [EntityAttributeData] and [EntityTypeData] respectively. */
    @JvmRecord
    data class Data(val attributes: EntityAttributeBank, val types: EntityTypesBank)

    fun nextUpdateFlag() { this.updateFlag++ }

    fun getContainer(type: EntityType<out LivingEntity>, entity: LivingEntity): AttributeContainer = this.handler.getContainer(type, entity)

    override fun load(manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Data> = CompletableFuture.supplyAsync({ ->
        val attributes = mutableMapOf<Identifier, EntityAttributeData>()
        val types = mutableMapOf<Identifier, EntityTypeData>()

        loadOverrides(manager, attributes)
        loadFunctions(manager, attributes)
        loadEntityTypes(manager, types)

        Data(attributes, types)
    }, executor)

    override fun apply(data: Data, manager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture<Void> = CompletableFuture.runAsync({ ->
        this.attributes = data.attributes
        this.types = data.types
        this.onDataUpdate()
    }, executor)

    /** Whenever new [Data] is applied from the [apply] phase. */
    fun onDataUpdate() {
        MutableRegistryImpl.unregister(Registries.ATTRIBUTE)

        for (id in Registries.ATTRIBUTE.ids) {
            val attribute = Registries.ATTRIBUTE[id] ?: continue
            (attribute as MutableEntityAttribute).`data_attributes$clear`()
        }

        for ((identifier, attributeData) in this.attributes) {
            attributeData.override(identifier, ::getOrCreate)
        }

        // todo: investigate if two are needed

        for ((identifier, attributeData) in this.attributes) {
            attributeData.copy(Registries.ATTRIBUTE[identifier] ?: continue)
        }

        this.handler.buildContainers(this.types, ENTITY_TYPE_INSTANCES)
    }

    override fun getFabricId() = ID
}