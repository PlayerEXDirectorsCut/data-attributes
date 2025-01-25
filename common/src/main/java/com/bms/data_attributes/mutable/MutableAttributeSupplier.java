package com.bms.data_attributes.mutable;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public interface MutableAttributeSupplier {

    /** Copies attribute configurations from a {@link AttributeSupplier.Builder}. */
    default void data_attributes$copy(AttributeSupplier.Builder builder) {};
}
