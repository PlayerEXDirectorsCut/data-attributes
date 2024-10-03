package com.bibireden.data_attributes.config

import com.bibireden.data_attributes.config.AttributeConfigManager.Companion.ENTITY_TYPE_INSTANCES
import com.bibireden.data_attributes.config.AttributeConfigManager.Tuple
import com.bibireden.data_attributes.config.entities.EntityTypeData
import com.bibireden.data_attributes.mutable.MutableAttributeContainer
import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.AttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.DefaultAttributeRegistry
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import org.jetbrains.annotations.ApiStatus
import kotlin.math.round

typealias ImplicitContainers = Map<Int, Tuple<DefaultAttributeContainer>>
typealias ExplicitContainers = Map<EntityType<out LivingEntity>, DefaultAttributeContainer>

/**
 * Class meant to obtain [AttributeContainer]'s from built [DefaultAttributeContainer]'s.
 *
 * It is complimentary to the [AttributeConfigManager] as it is supplies its needed containers from
 * its config [AttributeConfigManager.Data].
 *
 * This is primarily useful to be an internal implementation to apply config
 * information to [LivingEntity]'s in the game.
 */
@ApiStatus.Internal
class AttributeContainerHandler(private var implicitContainers: ImplicitContainers = mapOf(), private var explicitContainers: ExplicitContainers = mapOf()) {
    /**
     * Obtains a [MutableAttributeContainer] based on the provided [EntityType] and [LivingEntity].
     * A [DefaultAttributeContainer.Builder] is created and is copied through the containers, and is then finally built into a [AttributeContainer].
     *
     * @return [AttributeContainer] The obtained container applied to the given [LivingEntity].
     */
    fun getContainer(entityType: EntityType<out LivingEntity>, entity: LivingEntity): AttributeContainer {
        val builder = DefaultAttributeContainer.Builder()
        (DefaultAttributeRegistry.get(entityType) as? MutableDefaultAttributeContainer)?.`data_attributes$copy`(builder)

        this.implicitContainers.values.forEach { (type, container) ->
            if (type.isInstance(entity)) (container as MutableDefaultAttributeContainer).`data_attributes$copy`(builder)
        }

        (this.explicitContainers[entityType] as? MutableDefaultAttributeContainer)?.`data_attributes$copy`(builder)

        val container = AttributeContainer(builder.build()) as MutableAttributeContainer
        container.`data_attributes$setLivingEntity`(entity)
        return container as AttributeContainer
    }

    /**
     * Builds [ImplicitContainers] and [ExplicitContainers] from [EntityTypeData] and instance data.
     * Upon being built it would be ready to be obtained from the [getContainer] function to provide built [AttributeContainer]'s.
     *
     * @param entries The provided [EntityTypeData]
     * usually coming from a config/data source to provide default base values for the [LivingEntity] it represents.
     * @param instances Normally provided by [ENTITY_TYPE_INSTANCES], these are mapped class instances of [LivingEntity] and derived types.
     */
    @Suppress("UNCHECKED_CAST")
    fun buildContainers(entries: Map<Identifier, EntityTypeData>, instances: Map<Identifier, Tuple<Int>> = ENTITY_TYPE_INSTANCES) {
        val entityTypes = Registries.ENTITY_TYPE.ids.filter { DefaultAttributeRegistry.hasDefinitionFor(Registries.ENTITY_TYPE[it]) }.toSet()

        val implicits = mutableMapOf<Int, Tuple<DefaultAttributeContainer>>()
        val explicits = mutableMapOf<EntityType<out LivingEntity>, DefaultAttributeContainer>()

        val orderedEntityTypes = mutableMapOf<Int, Tuple<Identifier>>()

        entries.forEach { (identifier, data) ->
            val entry = instances.get(identifier)
            if (entry != null) orderedEntityTypes[entry.value] = Tuple(entry.livingEntity, identifier)

            if (!entityTypes.contains(identifier)) return@forEach

            val entityType = Registries.ENTITY_TYPE[identifier] as EntityType<out LivingEntity>

            val builder = DefaultAttributeContainer.Builder()
            data.build(builder, DefaultAttributeRegistry.get(entityType))
            explicits[entityType] = builder.build()
        }

        val size = orderedEntityTypes.size
        val max = orderedEntityTypes.keys.maxOrNull() ?: 0

        orderedEntityTypes.forEach { (hierarchy, tuple) ->
            val (entity, identifier) = tuple

            val index = round(size.toDouble() * hierarchy / max) - 1

            val builder = DefaultAttributeContainer.Builder()
            val entityTypeData = entries[identifier] ?: return@forEach
            entityTypeData.build(builder, null)
            implicits[index.toInt()] = Tuple(entity, builder.build())
        }

        this.implicitContainers = implicits
        this.explicitContainers = explicits
    }
}