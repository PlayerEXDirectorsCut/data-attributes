package com.bibireden.data_attributes.mutable;

import com.bibireden.data_attributes.api.util.VoidConsumer;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public interface MutableAttributeInstance {

    /** Returns the unique identifier associated with this instance. */
    default Identifier data_attributes$get_id() { return null; }

    /** Executes an action involving the instance and a provided {@link EntityAttributeModifier}. */
    default void data_attributes$actionModifier(final VoidConsumer consumerIn, final EntityAttributeInstance instanceIn, final EntityAttributeModifier modifierIn, final boolean isWasAdded) {}

    /** Sets a callback for changes to the associated {@link AttributeContainer}. */
    default void data_attributes$setContainerCallback(final AttributeContainer containerIn) {}

    /** Updates the identifier of the instance. */
    default void data_attributes$updateId(final Identifier identifierIn) {}

    /** Updates the instance. */
    default void data_attributes$refresh() {}
}
