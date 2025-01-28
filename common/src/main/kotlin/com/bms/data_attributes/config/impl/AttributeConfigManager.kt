package com.bms.data_attributes.config.impl

import com.bms.data_attributes.DataAttributes
import com.bms.data_attributes.api.EntityInstances
import com.bms.data_attributes.api.attribute.IAttribute
import com.bms.data_attributes.api.event.AttributesReloadedEvent
import com.bms.data_attributes.config.Cache
import com.bms.data_attributes.config.entry.ConfigMerger
import com.bms.data_attributes.config.functions.AttributeFunction
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride
import com.bms.data_attributes.data.AttributeData
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.endec.Endecs
import com.bms.data_attributes.ext.keyOf
import com.bms.data_attributes.mutable.MutableAttribute
import com.bms.data_attributes.networking.NetworkingChannels
import io.wispforest.endec.Endec
import io.wispforest.endec.StructEndec
import io.wispforest.endec.impl.StructEndecBuilder
import io.wispforest.owo.serialization.CodecUtils
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster

/**
 * Used to manage config data, and contains an [AttributeMapHandler] to build related [EntityTypeData].
 */
class AttributeConfigManager(var data: Data = Data(), private val handler: AttributeMapHandler = AttributeMapHandler()) {
    var updateFlag: Int = 0

    var defaults: Cache = Cache()

    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

    @JvmRecord
    data class Packet(val data: Data, val updateFlag: Int) : CustomPacketPayload {
        companion object {
            @JvmField
            val ENDEC: StructEndec<Packet> = StructEndecBuilder.of(
                Data.ENDEC.fieldOf("data") { it.data },
                Endec.INT.fieldOf("updateFlag") { it.updateFlag },
                AttributeConfigManager::Packet
            )

            val PACKET_ID = CustomPacketPayload.Type<Packet>(NetworkingChannels.RELOAD)
            val PACKET_CODEC: StreamCodec<FriendlyByteBuf, Packet> = CodecUtils.toPacketCodec(ENDEC)
        }

        override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = PACKET_ID
    }

    /**
     * Wrapper for the config manager to use internally to send as [Packet] data and to reflect changes based on what it contains.
     */
    data class Data(
        var overrides: Map<ResourceLocation, AttributeOverride> = mapOf(),
        var functions: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>> = mapOf(),
        var entityTypes: Map<ResourceLocation, EntityTypeData> = mapOf()
    )
    {
        companion object {
            val ENDEC: StructEndec<Data> = StructEndecBuilder.of(
                Endecs.RESOURCE.keyOf(AttributeOverride.ENDEC).fieldOf("overrides") { it.overrides },
                Endecs.RESOURCE.keyOf(Endecs.RESOURCE.keyOf(AttributeFunction.ENDEC)).fieldOf("functions") { it.functions },
                Endecs.RESOURCE.keyOf(EntityTypeData.ENDEC).fieldOf("entity_types") { it.entityTypes },
                AttributeConfigManager::Data
            )
        }
    }

    companion object {
        /**
         * Obtains an [Attribute] from the [BuiltInRegistries.ATTRIBUTE] registry.
         * Will return `null` if the [ResourceLocation] is not present.
         */
        fun getAttribute(identifier: ResourceLocation): Attribute? {
            return BuiltInRegistries.ATTRIBUTE[identifier]
        }

        val ENTITY_TYPE_INSTANCES = mapOf(
            EntityInstances.LIVING     to Tuple(LivingEntity::class.java, 0),
            EntityInstances.MOB        to Tuple(Mob::class.java, 1),
            EntityInstances.PATHFINDER to Tuple(PathfinderMob::class.java, 2),
            EntityInstances.HOSTILE    to Tuple(Monster::class.java, 3),
            EntityInstances.PASSIVE    to Tuple(AgeableMob::class.java, 4),
            EntityInstances.ANIMAL     to Tuple(Animal::class.java, 5)
        )
    }

    /** Currently applied [AttributeOverride]'s mapped with an [Attribute]'s [ResourceLocation]. */
    val overrides: Map<ResourceLocation, AttributeOverride>
        get() = this.data.overrides

    /** Currently applied [AttributeFunction]'s tied to the parent [Attribute]'s [ResourceLocation]. */
    val functions: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>
        get() = this.data.functions

    /** Currently applied [EntityTypeData] tied to an [EntityType]'s [ResourceLocation]. */
    val entityTypes: Map<ResourceLocation, EntityTypeData>
        get() = this.data.entityTypes

    /**
     * Increments to the next flag, usually signaling an update from the manager.
     * @return [Int] The update flag's current value.
     */
    fun nextUpdateFlag() = this.updateFlag++

    /**
     * Updates the data with the latest from the provided config.
     * This applies the data immediately afterward.
     */
    fun update() {
        this.data.overrides = ConfigMerger.mergeOverrides(defaults.overrides.entries)
        this.data.functions = ConfigMerger.mergeFunctions(defaults.functions.entries)
        this.data.entityTypes = ConfigMerger.mergeEntityTypes(defaults.types.entries)

        this.onDataUpdate()
    }

    /** Converts this manager to a sync-able packet. */
    fun toPacket() = Packet(data, updateFlag)

    /** Reads in the packet and applies fresh data and sets the update flag. */
    fun readPacket(packet: Packet) {
        this.data.overrides = packet.data.overrides
        this.data.functions = packet.data.functions
        this.data.entityTypes = packet.data.entityTypes

        this.updateFlag = packet.updateFlag
    }

    /**
     * Gets an [AttributeMap] based on the given [EntityType] and the provided [LivingEntity].
     * Useful for constructing a container based on the handler's state.
     */
    fun getContainer(type: EntityType<out LivingEntity>, entity: LivingEntity): AttributeMap = this.handler.getContainer(type, entity)

    /** Whenever new [Data] is applied. */
    fun onDataUpdate() {
        val attributeData = mutableMapOf<ResourceLocation, AttributeData>()

        insertOverrides(this.overrides, attributeData)
        insertFunctions(this.functions, attributeData)

        for (id in BuiltInRegistries.ATTRIBUTE.keySet()) {
            (BuiltInRegistries.ATTRIBUTE[id] as? MutableAttribute)?.`data_attributes$clear`()
        }

        for ((id, data) in attributeData) {
            data.override(getAttribute(id)!!) // was already asserted to exist in L: 124
        }

        for ((identifier, data) in attributeData) {
            data.copy(BuiltInRegistries.ATTRIBUTE[identifier]!!) // was already asserted to exist in L: 124
        }

        this.handler.buildContainers(this.entityTypes)

        AttributesReloadedEvent.EVENT.invoker().onReloadCompleted()

        DataAttributes.LOGGER.info("Updated manager with {} entries & {} entity-types. :: update flag [#{}]", attributeData.size, this.entityTypes.size, updateFlag)
    }

    private fun insertOverrides(overrides: Map<ResourceLocation, AttributeOverride>, attributeData: MutableMap<ResourceLocation, AttributeData>) {
        for ((id, override) in overrides) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                DataAttributes.LOGGER.warn("Attribute [$id] that was targeted for override is not registered. This has been skipped.")
                continue
            }
            val attribute = BuiltInRegistries.ATTRIBUTE[id]!! as IAttribute
            if (override.max.isNaN()) {
                override.max = attribute.`data_attributes$max_fallback`()
            }
            if (override.min.isNaN()) {
                override.min = attribute.`data_attributes$min_fallback`()
            }
            attributeData[id] = AttributeData(override)
        }
    }

    private fun insertFunctions(store: Map<ResourceLocation, Map<ResourceLocation, AttributeFunction>>, data: MutableMap<ResourceLocation, AttributeData>) {
        for ((id, functions) in store) {
            if (!BuiltInRegistries.ATTRIBUTE.containsKey(id)) {
                DataAttributes.LOGGER.warn("Function parent [$id] that was defined in config is not registered. This has been skipped.")
            } else {
                val dat = data[id] ?: AttributeData()
                dat.putFunctions(functions)
                data[id] = dat
            }
        }
    }
}