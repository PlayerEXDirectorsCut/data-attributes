package com.bms.data_attributes.api

import com.bms.data_attributes.DataAttributes.Companion.id
import net.minecraft.world.entity.LivingEntity

object EntityInstances {
    /** The entity instance for [LivingEntity]. */
    @JvmField
    val LIVING = id("living_entity")

    /** The entity instance for [net.minecraft.world.entity.Mob]. */
    @JvmField
    val MOB = id("mob_entity")

    /** The entity instance for [net.minecraft.world.entity.PathfinderMob]. */
    @JvmField
    val PATHFINDER = id("path_aware_entity")

    /** The entity instance for [net.minecraft.world.entity.monster.Monster]. */
    @JvmField
    val HOSTILE = id("hostile_entity")

    /** The entity instance for [net.minecraft.world.entity.AgeableMob]. */
    @JvmField
    val PASSIVE = id("passive_entity")

    /** The entity instance for [net.minecraft.world.entity.animal.Animal]. */
    @JvmField
    val ANIMAL = id("animal_entity")
}