package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.math.round

class AttributeContainerHandler(
    private var implicitContainers: Map<Int, Tuple<DefaultAttributeContainer>> = mutableMapOf(),
    private var explicitContainers: Map<EntityType<out LivingEntity>, DefaultAttributeContainer> = mutableMapOf()
) {
    companion object {
        data class Tuple<T>(val entity: Class<out LivingEntity>, val value: T)
    }

    fun getContainer(entityType: EntityType<out LivingEntity>, livingEntity: LivingEntity) : AttributeContainer {
        val builder = DefaultAttributeContainer.builder()

        this.implicitContainers.values.forEach {tuple ->
            val tupleEntity = tuple.entity
            
            if (tupleEntity.isInstance(livingEntity)) {
                (tuple.value as MutableDefaultAttributeContainer).copy(builder)
            }
        }

        this.explicitContainers[entityType]?.let { container ->
            (container as MutableDefaultAttributeContainer).copy(builder)
        }

        val container = AttributeContainer(builder.build())
        (container as MutableAttributeContainer).setLivingEntity(livingEntity)

        return container
    }

    @Suppress("UNCHECKED_CAST")
    fun buildContainers(registry: DataAttributesRegistry, entityTypeInstances: Map<Identifier, Tuple<Int>>) {
        val entityTypes = Registries.ENTITY_TYPE.ids.filter { id ->
            DefaultAttributeRegistry.hasDefinitionFor(Registries.ENTITY_TYPE[id])
        }
        val newImplicitContainers = mutableMapOf<Int, Tuple<DefaultAttributeContainer>>()
        val newExplicitContainers = mutableMapOf<EntityType<out LivingEntity>, DefaultAttributeContainer>()
        val orderedEntityTypes = mutableMapOf<Int, Tuple<Identifier>>()

        registry.entityTypeData.forEach { (id, data) ->
            val tuple = entityTypeInstances[id] ?: return@forEach
            orderedEntityTypes[tuple.value] = Tuple(tuple.entity, id)

            val entityType = Registries.ENTITY_TYPE[id] as EntityType<out LivingEntity>
            val builder = DefaultAttributeContainer.builder()
            data.build(builder, DefaultAttributeRegistry.get(entityType))
            newExplicitContainers[entityType] = builder.build()
        }

        val size = orderedEntityTypes.size
        val max = orderedEntityTypes.keys.maxOrNull() ?: 0

        for ((hierarchy, tuple) in orderedEntityTypes) {
            val index = (round(size.toDouble() * hierarchy / max)).toInt() - 1
            val builder = DefaultAttributeContainer.builder()
            val entityTypeData = registry.entityTypeData[tuple.value] ?: continue
            entityTypeData.build(builder, null)
            newImplicitContainers[index] = Tuple(tuple.entity, builder.build())
        }

        this.implicitContainers = newImplicitContainers
        this.explicitContainers = newExplicitContainers
    }
}