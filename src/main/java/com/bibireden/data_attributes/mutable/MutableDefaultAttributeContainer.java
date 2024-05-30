package com.bibireden.data_attributes.mutable;

import net.minecraft.entity.attribute.DefaultAttributeContainer;

public interface MutableDefaultAttributeContainer {

    // Copies attribute configurations from a DefaultAttributeContainer.Builder
    void copy(DefaultAttributeContainer.Builder builder);
}
