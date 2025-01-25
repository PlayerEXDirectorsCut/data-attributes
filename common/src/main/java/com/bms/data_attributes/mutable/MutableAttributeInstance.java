package com.bms.data_attributes.mutable;

import com.bms.data_attributes.api.util.VoidConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public interface MutableAttributeInstance {

    /** Returns the unique identifier associated with this instance. */
    default ResourceLocation data_attributes$get_id() { return null; }

    /** Executes an action involving the instance and a provided {@link AttributeModifier}. */
    default void data_attributes$actionModifier(final VoidConsumer consumerIn, final AttributeInstance instanceIn, final AttributeModifier modifierIn, final boolean isWasAdded) {}

    /** Sets a callback for changes to the associated {@link AttributeMap}. */
    default void data_attributes$setContainerCallback(final AttributeMap mapIn) {}

    /** Updates the identifier of the instance. */
    default void data_attributes$updateId(final ResourceLocation identifierIn) {}

    /** Updates the instance. */
    default void data_attributes$refresh() {}
}
