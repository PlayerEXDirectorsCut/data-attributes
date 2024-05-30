package com.bibireden.data_attributes.mutable;

import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.util.Identifier;

public interface MutableAttributeContainer {

    // Returns a map of custom attributes associated with this container
    Map<Identifier, EntityAttributeInstance> custom();

    // Returns the LivingEntity associated with this container
    LivingEntity getLivingEntity();

    // Sets the LivingEntity associated with this container
    void setLivingEntity(final LivingEntity livingEntity);

    // Refreshes the attributes of the associated LivingEntity
    void refresh();

    // Clears any tracked information (possibly related to attribute changes)
    void clearTracked();
}
