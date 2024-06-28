package com.bibireden.data_attributes.api

import com.bibireden.data_attributes.DataAttributes.Companion.id
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.PathAwareEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.passive.PassiveEntity

object EntityInstances {
    /** The entity instance for [LivingEntity]. */
    @JvmField
    val LIVING = id("living_entity")

    /** The entity instance for [MobEntity]. */
    @JvmField
    val MOB = id("mob_entity")

    /** The entity instance for [PathAwareEntity]. */
    @JvmField
    val PATH_AWARE = id("path_aware_entity")

    /** The entity instance for [HostileEntity]. */
    @JvmField
    val HOSTILE = id("hostile_entity")

    /** The entity instance for [PassiveEntity]. */
    @JvmField
    val PASSIVE = id("passive_entity")

    /** The entity instance for [AnimalEntity]. */
    @JvmField
    val ANIMAL = id("animal_entity")
}