package com.bibireden.data_attributes.mutable;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.util.Identifier;

public interface MutableAttributeContainer {

    /** Returns a map of custom {@link EntityAttributeInstance}'s associated with this container. */
    default Map<Identifier, EntityAttributeInstance> data_attributes$custom() { return new HashMap<>(); }

    /** Returns the {@link LivingEntity} associated with this container. */
    default LivingEntity data_attributes$getLivingEntity() { return null; }

    /** Sets the {@link LivingEntity} associated with this container. */
    default void data_attributes$setLivingEntity(final LivingEntity livingEntity) {}

    /** Refreshes the attributes of the associated {@link LivingEntity}. */
    default void data_attributes$refresh() {}

    /** Clears any tracked information (possibly related to attribute changes). */
    default void data_attributes$clearTracked() {}
}
