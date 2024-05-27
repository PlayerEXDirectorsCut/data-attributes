package com.bibireden.data_attributes.api

import com.bibireden.data_attributes.DataAttributes
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object DataAttributesAPI {
    val MOD_ID = DataAttributes.MOD_ID
    /**
     * The entity instance for LivingEntity.class.
     */
    const val ENTITY_INSTANCE_LIVING_ENTITY: String = "living_entity"

    /**
     * The entity instance for MobEntity.class.
     */
    const val ENTITY_INSTANCE_MOB_ENTITY: String = "mob_entity"

    /**
     * The entity instance for PathAwareEntity.class.
     */
    const val ENTITY_INSTANCE_PATH_AWARE_ENTITY: String = "path_aware_entity"

    /**
     * The entity instance for HostileEntity.class.
     */
    const val ENTITY_INSTANCE_HOSTILE_ENTITY: String = "hostile_entity"

    /**
     * The entity instance for PassiveEntity.class.
     */
    const val ENTITY_INSTANCE_PASSIVE_ENTITY: String = "passive_entity"

    /**
     * The entity instance for AnimalEntity.class.
     */
    const val ENTITY_INSTANCE_ANIMAL_ENTITY: String = "animal_entity"

    /**
     * // todo: this is probably not applicable anymore due to DynamicRegistries.
     *
     * Obtains a `EntityAttribute` supplier.
     *
     * The reason behind this is that static initialization may not work with the datapacks, so this static method would be safer.
     */
    fun getAttribute(key: Identifier): () -> EntityAttribute? = { -> Registries.ATTRIBUTE[key] }

    fun <T> ifPresent(entity: LivingEntity, attributeSupplier: () -> EntityAttribute?, fallback: () -> T, onAttributePresent: (Double) -> T): T {
        val container = entity.attributes
        val attribute = attributeSupplier()

        if (attribute != null && container?.hasAttribute(attribute) == true) {
            return onAttributePresent(container.getValue(attribute))
        }
        return fallback()
    }
}