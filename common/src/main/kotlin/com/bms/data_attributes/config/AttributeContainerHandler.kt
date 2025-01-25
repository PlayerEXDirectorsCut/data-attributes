package com.bms.data_attributes.config

import com.bms.data_attributes.config.AttributeConfigManager.Companion.ENTITY_TYPE_INSTANCES
import com.bms.data_attributes.config.AttributeConfigManager.Tuple
import com.bms.data_attributes.config.entities.EntityTypeData
import com.bms.data_attributes.mutable.MutableAttributeMap
import com.bms.data_attributes.mutable.MutableAttributeSupplier
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus
import kotlin.math.round

typealias ImplicitContainers = Map<Int, Tuple<AttributeSupplier>>
typealias ExplicitContainers = Map<EntityType<out LivingEntity>, AttributeSupplier>

/**
 * Class meant to obtain [AttributeMap]'s from built [AttributeSupplier]'s.
 *
 * It is complimentary to the [AttributeConfigManager] as it is supplies its needed containers from
 * its config [AttributeConfigManager.Data].
 *
 * This is primarily useful to be an internal implementation to apply config
 * information to [LivingEntity]'s in the game.
 */
@ApiStatus.Internal
class AttributeMapHandler(private var implicitContainers: ImplicitContainers = mapOf(), private var explicitContainers: ExplicitContainers = mapOf()) {
    /**
     * Obtains a [MutableAttributeMap] based on the provided [EntityType] and [LivingEntity].
     * A [AttributeSupplier.Builder] is created and is copied through the containers, and is then finally built into a [AttributeMap].
     *
     * @return [AttributeMap] The obtained container applied to the given [LivingEntity].
     */
    fun getContainer(entityType: EntityType<out LivingEntity>, entity: LivingEntity): AttributeMap {
        val builder = AttributeSupplier.Builder()
        (DefaultAttributes.getSupplier(entityType) as? MutableAttributeSupplier)?.`data_attributes$copy`(builder)

        this.implicitContainers.values.forEach { (type, container) ->
            if (type.isInstance(entity)) (container as MutableAttributeSupplier).`data_attributes$copy`(builder)
        }

        (this.explicitContainers[entityType] as? MutableAttributeSupplier)?.`data_attributes$copy`(builder)

        val container = AttributeMap(builder.build()) as MutableAttributeMap
        container.`data_attributes$setLivingEntity`(entity)
        return container as AttributeMap
    }

    /**
     * Builds [ImplicitContainers] and [ExplicitContainers] from [EntityTypeData] and instance data.
     * Upon being built it would be ready to be obtained from the [getContainer] function to provide built [AttributeMap]'s.
     *
     * @param entries The provided [EntityTypeData]
     * usually coming from a config/data source to provide default base values for the [LivingEntity] it represents.
     * @param instances Normally provided by [ENTITY_TYPE_INSTANCES], these are mapped class instances of [LivingEntity] and derived types.
     */
    @Suppress("UNCHECKED_CAST")
    fun buildContainers(entries: Map<ResourceLocation, EntityTypeData>, instances: Map<ResourceLocation, Tuple<Int>> = ENTITY_TYPE_INSTANCES) {
        val entityTypes = BuiltInRegistries.ENTITY_TYPE.keySet().filter { DefaultAttributes.hasSupplier(BuiltInRegistries.ENTITY_TYPE[it]) }.toSet()

        val implicits = mutableMapOf<Int, Tuple<AttributeSupplier>>()
        val explicits = mutableMapOf<EntityType<out LivingEntity>, AttributeSupplier>()

        val orderedEntityTypes = mutableMapOf<Int, Tuple<ResourceLocation>>()

        entries.forEach { (identifier, data) ->
            val entry = instances[identifier]
            if (entry != null) orderedEntityTypes[entry.value] = Tuple(entry.livingEntity, identifier)

            if (!entityTypes.contains(identifier)) return@forEach

            val entityType = BuiltInRegistries.ENTITY_TYPE[identifier] as EntityType<out LivingEntity>

            val builder = AttributeSupplier.Builder()
            data.build(builder, DefaultAttributes.getSupplier(entityType))
            explicits[entityType] = builder.build()
        }

        val size = orderedEntityTypes.size
        val max = orderedEntityTypes.keys.maxOrNull() ?: 0

        orderedEntityTypes.forEach { (hierarchy, tuple) ->
            val (entity, identifier) = tuple

            val index = round(size.toDouble() * hierarchy / max) - 1

            val builder = AttributeSupplier.Builder()
            val entityTypeData = entries[identifier] ?: return@forEach
            entityTypeData.build(builder, null)
            implicits[index.toInt()] = Tuple(entity, builder.build())
        }

        this.implicitContainers = implicits
        this.explicitContainers = explicits
    }
}