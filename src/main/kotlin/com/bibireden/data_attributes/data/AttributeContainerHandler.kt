package com.bibireden.data_attributes.data

import com.bibireden.data_attributes.config.AttributeConfigManager.Tuple
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

typealias ImplicitContainers = Map<Int, Tuple<DefaultAttributeContainer>>
typealias ExplicitContainers = Map<EntityType<out LivingEntity>, DefaultAttributeContainer>

/** Used to handle */
class AttributeContainerHandler(private var implicitContainers: ImplicitContainers = mapOf(), private var explicitContainers: ExplicitContainers = mapOf()) {
    /**
     * Obtains a [MutableAttributeContainer] based on the provided `entityType` and [LivingEntity].
     * A [DefaultAttributeContainer.Builder] is created and is copied through the containers, and is then finally built into a [AttributeContainer].
     *
     * @return [AttributeContainer]
     */
    fun getContainer(entityType: EntityType<out LivingEntity>, livingEntity: LivingEntity): AttributeContainer {
        val builder = DefaultAttributeContainer.Builder()
        (DefaultAttributeRegistry.get(entityType) as? MutableDefaultAttributeContainer)?.`data_attributes$copy`(builder)

        this.implicitContainers.values.forEach { (type, container) ->
            if (type.isInstance(livingEntity)) (container as MutableDefaultAttributeContainer).`data_attributes$copy`(builder)
        }

        (this.explicitContainers[entityType] as? MutableDefaultAttributeContainer)?.`data_attributes$copy`(builder)

        val container = AttributeContainer(builder.build()) as MutableAttributeContainer
        container.`data_attributes$setLivingEntity`(livingEntity)
        return container as AttributeContainer
    }

    @Suppress("UNCHECKED_CAST")
    fun buildContainers(entityTypeDataIn: Map<Identifier, EntityTypeData>, instances: Map<Identifier, Tuple<Int>>) {
        val entityTypes = Registries.ENTITY_TYPE.ids.filter { DefaultAttributeRegistry.hasDefinitionFor(Registries.ENTITY_TYPE[it]) }.toSet()

        val implicits = mutableMapOf<Int, Tuple<DefaultAttributeContainer>>()
        val explicits = mutableMapOf<EntityType<out LivingEntity>, DefaultAttributeContainer>()

        val orderedEntityTypes = mutableMapOf<Int, Tuple<Identifier>>()

        entityTypeDataIn.forEach { (identifier, data) ->
            val entry = instances.get(identifier)
            if (entry != null) {
                orderedEntityTypes[entry.value] = Tuple(entry.livingEntity, identifier)
            }

            if (!entityTypes.contains(identifier)) return@forEach

            val entityType = Registries.ENTITY_TYPE[identifier] as EntityType<out LivingEntity>
            val entityTypeData = entityTypeDataIn[identifier] ?: return@forEach



            val builder = DefaultAttributeContainer.Builder()
            entityTypeData.build(builder, DefaultAttributeRegistry.get(entityType))
            explicits[entityType] = builder.build()
        }

        val size = orderedEntityTypes.size
        val max = orderedEntityTypes.keys.maxOrNull() ?: 0

        orderedEntityTypes.forEach { (hierarchy, tuple) ->
            val (entity, identifier) = tuple

            val index = round(size.toDouble() * hierarchy / max) - 1

            val builder = DefaultAttributeContainer.Builder()
            val entityTypeData = entityTypeDataIn[identifier] ?: return@forEach
            entityTypeData.build(builder, null)
            implicits[index.toInt()] = Tuple(entity, builder.build())
        }

        this.implicitContainers = implicits
        this.explicitContainers = explicits
    }
}