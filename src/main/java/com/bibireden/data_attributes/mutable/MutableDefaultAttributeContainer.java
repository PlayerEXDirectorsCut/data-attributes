package com.bibireden.data_attributes.mutable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;

public interface MutableDefaultAttributeContainer {

    /** Copies attribute configurations from a {@link DefaultAttributeContainer.Builder}. */
    default void data_attributes$copy(DefaultAttributeContainer.Builder builder) {};
}
