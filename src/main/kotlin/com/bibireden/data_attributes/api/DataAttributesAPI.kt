package com.bibireden.data_attributes.api

import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.*

private typealias AttributeSupplier = () -> Optional<EntityAttribute>

object DataAttributesAPI {
    @JvmField
    val MOD_ID = "data_attributes"

    /** Creates an identifier based on the API. */
    fun id(str: String) = Identifier.of(MOD_ID, str)!!

    /**
     * The entity instance for LivingEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_LIVING_ENTITY = id("living_entity")

    /**
     * The entity instance for MobEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_MOB_ENTITY = id("mob_entity")

    /**
     * The entity instance for PathAwareEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_PATH_AWARE_ENTITY = id("path_aware_entity")

    /**
     * The entity instance for HostileEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_HOSTILE_ENTITY = id("hostile_entity")

    /**
     * The entity instance for PassiveEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_PASSIVE_ENTITY = id("passive_entity")

    /**
     * The entity instance for AnimalEntity.class.
     */
    @JvmField
    val ENTITY_INSTANCE_ANIMAL_ENTITY = id("animal_entity")

    /**
     * Gets a [Function] that will provide a registered attribute assigned to the given key.
     *
     * Static initialization is not possible for these attributes, which is the reason why this is handled differently.
     */
    fun getAttribute(key: Identifier): AttributeSupplier = { -> Optional.ofNullable(Registries.ATTRIBUTE[key]) }

    /**
     * Tries to obtain a [EntityAttribute] off a [LivingEntity]. Certain requirements must be met in order for the value to be present:
     *
     * - The attribute is registered to the game
     * - The attribute is **present** on the given [LivingEntity].
     *
     * The returned [Optional] provides more versatility and fallback abilities.
     */
    fun getValue(entity: LivingEntity, supplier: AttributeSupplier): Optional<Double> {
        val container = entity.attributes
        val attribute = supplier()

        if (!attribute.isEmpty && container.hasAttribute(attribute.get())) {
            return Optional.of(container.getValue(attribute.get()))
        }
        else {
            return Optional.empty()
        }
    }
}