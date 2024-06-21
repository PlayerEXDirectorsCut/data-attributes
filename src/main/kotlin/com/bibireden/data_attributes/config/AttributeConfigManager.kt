package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.api.EntityInstances
import com.bibireden.data_attributes.api.event.AttributesReloadedEvent
import com.bibireden.data_attributes.config.OverridesConfigModel.AttributeOverrideConfig
import com.bibireden.data_attributes.config.data.EntityTypesConfigData
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

class AttributeConfigManager(
    val data: AttributeData = AttributeData(),
    val handler: AttributeContainerHandler = AttributeContainerHandler(),
) {
    @JvmRecord
    data class Tuple<T>(val livingEntity: Class<out LivingEntity>, val value: T)

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

    var updateFlag = 0

    companion object
    {
        @JvmField
        val ENDEC = AttributeData.ENDEC.xmap(::AttributeConfigManager) { it.data }

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
    fun updateEntityTypes(config: EntityTypesConfigData)
    {
        this.data.entity_types.putAll(config.data)
        onDataUpdate()
    }

    /** Whenever new [AttributeData] is applied. */
    fun onDataUpdate() {
        val entityAttributeData = mutableMapOf<Identifier, EntityAttributeData>()
        val entityTypeData = mutableMapOf<Identifier, EntityTypeData>()

        for ((key, value) in this.data.overrides) {
            entityAttributeData[key] = EntityAttributeData(AttributeOverrideConfig())
        }

        for ((id, value) in this.data.entity_types) {
            entityTypeData[id] = value
        }

        MutableRegistryImpl.unregister(Registries.ATTRIBUTE)

        for (id in Registries.ATTRIBUTE.ids) {
            val attribute = Registries.ATTRIBUTE[id] ?: continue
            (attribute as MutableEntityAttribute).`data_attributes$clear`()
        }

        for ((identifier, attributeData) in entityAttributeData) {
            attributeData.override(identifier, ::getOrCreate)
            attributeData.copy(Registries.ATTRIBUTE[identifier] ?: continue)
        }

        this.handler.buildContainers(entityTypeData, ENTITY_TYPE_INSTANCES)

        AttributesReloadedEvent.EVENT.invoker().onCompletedReload()
    }
}