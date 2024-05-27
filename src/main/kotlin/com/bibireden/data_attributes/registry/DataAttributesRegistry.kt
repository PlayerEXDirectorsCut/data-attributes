package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.api.DataAttributesAPI
import com.bibireden.data_attributes.api.events.AttributesReloadedEvent
import com.bibireden.data_attributes.mutable.MutableEntityAttribute
import com.bibireden.data_attributes.registry.AttributeContainerHandler.Companion.Tuple
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


class DataAttributesRegistry(val skillAttributes: MutableMap<Identifier, SkillAttributeData> = mutableMapOf(), val entityTypeData: MutableMap<Identifier, EntityTypeData> = mutableMapOf()) {
    companion object {
        val ENTITY_TYPE_INSTANCES = mapOf(
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_LIVING_ENTITY) to Tuple(LivingEntity::class.java, 0),
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_MOB_ENTITY) to Tuple(MobEntity::class.java, 1),
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_PATH_AWARE_ENTITY) to Tuple(PathAwareEntity::class.java, 2),
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_HOSTILE_ENTITY) to Tuple(HostileEntity::class.java, 3),
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_PASSIVE_ENTITY) to Tuple(PassiveEntity::class.java, 4),
            Identifier(DataAttributesAPI.MOD_ID, DataAttributesAPI.ENTITY_INSTANCE_ANIMAL_ENTITY) to Tuple(AnimalEntity::class.java, 5)
        )

        private fun getOrCreate(identifier: Identifier, attributeIn: EntityAttribute): EntityAttribute {
            return Registries.ATTRIBUTE.get(identifier) ?: DynamicMutableRegistry.register(Registries.ATTRIBUTE, identifier, attributeIn)
        }
    }

    private val handler = AttributeContainerHandler()
    private var updateFlag = 0

    fun nextUpdateFlag() {
        this.updateFlag++
    }

    fun updateFlag() = this.updateFlag

    fun getContainer(entityType: EntityType<out LivingEntity>, livingEntity: LivingEntity): AttributeContainer {
        return this.handler.getContainer(entityType, livingEntity)
    }

    fun apply() {
        DynamicMutableRegistry.unregister(Registries.ATTRIBUTE)

        for (id in Registries.ATTRIBUTE.ids) {
            val entityAttribute = Registries.ATTRIBUTE.get(id) ?: continue
            (entityAttribute as MutableEntityAttribute).clear()
        }

        for ((id, skill) in this.skillAttributes) { skill.override(id, ::getOrCreate) }

        for ((id, skill) in this.skillAttributes) {
            skill.copy(Registries.ATTRIBUTE[id] ?: continue)
        }

        this.handler.buildContainers(this, ENTITY_TYPE_INSTANCES)

        AttributesReloadedEvent.EVENT.invoker().onReload()
    }

    fun clear() {
        this.skillAttributes.clear()
        this.entityTypeData.clear()
    }
}