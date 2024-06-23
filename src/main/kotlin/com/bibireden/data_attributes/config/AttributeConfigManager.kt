package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.api.EntityInstances
import com.bibireden.data_attributes.api.event.AttributesReloadedEvent
import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.data.*
import com.bibireden.data_attributes.endec.Endecs
import com.bibireden.data_attributes.impl.MutableRegistryImpl
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

class AttributeConfigManager(var data: AttributeData = AttributeData(), val handler: AttributeContainerHandler = AttributeContainerHandler(), var updateFlag: Int = 0) {
    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

    @JvmRecord
    data class Packet(val data: AttributeData, val updateFlag: Int) {
        companion object {
            @JvmField
            val ENDEC = StructEndecBuilder.of(
                AttributeData.ENDEC.fieldOf("data") { it.data },
                Endec.INT.fieldOf("updateFlag") { it.updateFlag },
                ::Packet
            )
        }
    }

    @JvmRecord
    data class AttributeData(
        val overrides: MutableMap<Identifier, AttributeOverrideConfig> = mutableMapOf(),
        val functions: MutableMap<Identifier, List<AttributeFunctionConfig>> = mutableMapOf(),
        val entity_types: MutableMap<Identifier, EntityTypeData> = mutableMapOf()
    )
    {
        companion object {
            val ENDEC = StructEndecBuilder.of(
                Endec.map(Endecs.IDENTIFIER, AttributeOverrideConfig.ENDEC).fieldOf("overrides") { it.overrides },
                Endec.map(Endecs.IDENTIFIER, AttributeFunctionConfig.ENDEC.listOf()).fieldOf("functions") { it.functions },
                Endec.map(Endecs.IDENTIFIER, EntityTypeData.ENDEC).fieldOf("entity_types") { it.entity_types },
                ::AttributeData
            )
        }
    }

    companion object
    {
        /**
         * This expects an attribute to be instantiated by the time this is called.
         * @throws IllegalStateException
         */
        fun expectAttribute(identifier: Identifier): EntityAttribute {
            return Registries.ATTRIBUTE[identifier] ?: throw IllegalStateException("Attribute $identifier is not registered! Please configure accordingly to resolve this issue.")
        }

        fun getOrCreate(identifier: Identifier, attribute: EntityAttribute): EntityAttribute
        {
            return Registries.ATTRIBUTE[identifier] ?: MutableRegistryImpl.register(Registries.ATTRIBUTE, identifier, attribute)
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

    /** Posts overrides and calls the [onDataUpdate] method for sync. */
    fun updateOverrides(config: Map<Identifier, AttributeOverrideConfig>)
    {
        this.data.overrides.putAll(config)
        onDataUpdate()
    }

    /** Posts functions and calls the [onDataUpdate] method for sync. */
    fun updateFunctions(config: AttributeFunctionConfigData)
    {
        this.data.functions.putAll(config.data)
        onDataUpdate()
    }

    /** Post entity types and calls the `onDataUpdate` method for sync.*/
    fun updateEntityTypes(config: Map<Identifier, EntityTypeData>)
    {
        this.data.entity_types.putAll(config)
        onDataUpdate()
    }

    /** Whenever new [AttributeData] is applied. */
    fun onDataUpdate() {
        val entityAttributeData = mutableMapOf<Identifier, EntityAttributeData>()
        val entityTypeData = mutableMapOf<Identifier, EntityTypeData>()
        val functions = mutableMapOf<Identifier, List<AttributeFunctionConfig>>()

        for ((key, value) in this.data.overrides) {
            entityAttributeData[key] = EntityAttributeData(value)
        }

        for ((id, value) in this.data.entity_types) {
            entityTypeData[id] = value
        }

        MutableRegistryImpl.unregister(Registries.ATTRIBUTE)

        for (id in Registries.ATTRIBUTE.ids) {
            val attribute = Registries.ATTRIBUTE[id] ?: continue
            (attribute as MutableEntityAttribute).`data_attributes$clear`()
        }

        for ((identifier, data) in entityAttributeData) {
            data.override(expectAttribute(identifier))
        }

        for ((identifier, attributeData) in entityAttributeData) {
            val attribute = Registries.ATTRIBUTE[identifier] ?: continue
            attributeData.copy(attribute)
        }

        this.handler.buildContainers(entityTypeData, ENTITY_TYPE_INSTANCES)

        AttributesReloadedEvent.EVENT.invoker().onCompletedReload()
    }
}