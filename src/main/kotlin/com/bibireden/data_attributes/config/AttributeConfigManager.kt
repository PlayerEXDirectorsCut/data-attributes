package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.DataAttributes
import com.bibireden.data_attributes.api.EntityInstances
import com.bibireden.data_attributes.api.event.AttributesReloadedEvent
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.AttributeFunction
import com.bibireden.data_attributes.data.EntityAttributeData
import com.bibireden.data_attributes.data.EntityTypeData
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import io.wispforest.endec.Endec
import io.wispforest.endec.impl.StructEndecBuilder
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
import net.minecraft.util.Identifier

class AttributeConfigManager(var data: Data = Data(), val handler: AttributeContainerHandler = AttributeContainerHandler(), var updateFlag: Int = 0) {
    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

    @JvmRecord
    data class Packet(val data: Data, val updateFlag: Int) {
        companion object {
            @JvmField
            val ENDEC = StructEndecBuilder.of(
                Data.ENDEC.fieldOf("data") { it.data },
                Endec.INT.fieldOf("updateFlag") { it.updateFlag },
                ::Packet
            )
        }
    }

    @JvmRecord
    data class Data(
        val overrides: MutableMap<Identifier, AttributeOverrideConfig> = mutableMapOf(),
        val functions: MutableMap<Identifier, List<AttributeFunction>> = mutableMapOf(),
        val entity_types: MutableMap<Identifier, EntityTypeData> = mutableMapOf()
    )
    {
        companion object {
            val ENDEC = StructEndecBuilder.of(
                Endec.map(Endecs.IDENTIFIER, AttributeOverrideConfig.ENDEC).fieldOf("overrides") { it.overrides },
                Endec.map(Endecs.IDENTIFIER, AttributeFunction.ENDEC.listOf()).fieldOf("functions") { it.functions },
                Endec.map(Endecs.IDENTIFIER, EntityTypeData.ENDEC).fieldOf("entity_types") { it.entity_types },
                ::Data
            )
        }
    }

    companion object
    {
        /**
         * Obtains an `EntityAttribute` from the [Registries.ATTRIBUTE] registry.
         * Will return `null` if the [Identifier] is not present.
         */
        fun getAttribute(identifier: Identifier): EntityAttribute? {
            return Registries.ATTRIBUTE[identifier]
        }

        val ENTITY_TYPE_INSTANCES = mapOf(
            EntityInstances.LIVING     to Tuple(LivingEntity::class.java, 0),
            EntityInstances.MOB        to Tuple(MobEntity::class.java, 1),
            EntityInstances.PATH_AWARE to Tuple(PathAwareEntity::class.java, 2),
            EntityInstances.HOSTILE    to Tuple(HostileEntity::class.java, 3),
            EntityInstances.PASSIVE    to Tuple(PassiveEntity::class.java, 4),
            EntityInstances.ANIMAL     to Tuple(AnimalEntity::class.java, 5)
        )
    }

    /**
     * Updates the data with the latest from the provided config.
     * This applies the data immediately afterward.
     */
    fun updateData() {
        this.data = Data(
            DataAttributes.OVERRIDES_CONFIG.overrides.toMutableMap(),
            DataAttributes.FUNCTIONS_CONFIG.functions.data.toMutableMap(),
            DataAttributes.ENTITY_TYPES_CONFIG.entity_types.toMutableMap()
        )
        this.onDataUpdate()
    }

    /** Converts this manager to a sync-able packet. */
    fun toPacket() = Packet(data, updateFlag)

    /** Reads in the packet and applies fresh data and sets the update flag. */
    fun readPacket(packet: Packet) {
        this.data = packet.data
        this.updateFlag = packet.updateFlag
    }

    fun nextUpdateFlag() {
        this.updateFlag++
    }

    fun getContainer(type: EntityType<out LivingEntity>, entity: LivingEntity): AttributeContainer = this.handler.getContainer(type, entity)

    /** Whenever new [Data] is applied. */
    fun onDataUpdate() {
        val entityAttributeData = mutableMapOf<Identifier, EntityAttributeData>()

        for ((id, value) in this.data.overrides) {
            if (!Registries.ATTRIBUTE.containsId(id)) {
                DataAttributes.LOGGER.warn("Attribute [$id] that was targeted for override is not registered. This has been skipped.")
                continue
            }
            entityAttributeData[id] = EntityAttributeData(value)
        }

        for ((id, configs) in this.data.functions) {
            if (!Registries.ATTRIBUTE.containsId(id)) {
                DataAttributes.LOGGER.warn("Function parent [$id] that was defined in config is not registered. This has been skipped.")
            }
            else {
                entityAttributeData.getOrPut(id, ::EntityAttributeData).putFunctions(configs)
            }
        }

        for (id in Registries.ATTRIBUTE.ids) {
            (Registries.ATTRIBUTE[id] as? MutableEntityAttribute)?.`data_attributes$clear`()
        }

        for ((id, data) in entityAttributeData) {
            data.override(getAttribute(id)!!) // was already asserted to exist in L: 127
        }

        for ((identifier, attributeData) in entityAttributeData) {
            Registries.ATTRIBUTE[identifier]?.let(attributeData::copy)
        }

        this.handler.buildContainers(this.data.entity_types, ENTITY_TYPE_INSTANCES)

        AttributesReloadedEvent.EVENT.invoker().onCompletedReload()
    }
}