package com.bms.data_attributes.mutable;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public interface MutableAttributeMap {

    /** Returns a map of custom {@link AttributeInstance}'s associated with this container. */
    default Map<ResourceLocation, AttributeInstance> data_attributes$custom() { return new HashMap<>(); }

    /** Returns the {@link LivingEntity} associated with this container. */
    default LivingEntity data_attributes$getLivingEntity() { return null; }

    /** Sets the {@link LivingEntity} associated with this container. */
    default void data_attributes$setLivingEntity(final LivingEntity livingEntity) {}

    /** Refreshes the attributes of the associated {@link LivingEntity}. */
    default void data_attributes$refresh() {}

    /** Clears any tracked information (possibly related to attribute changes). */
    default void data_attributes$clearTracked() {}
}
