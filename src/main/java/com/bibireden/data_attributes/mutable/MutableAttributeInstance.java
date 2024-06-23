package com.bibireden.data_attributes.mutable;

import com.bibireden.data_attributes.api.util.VoidConsumer;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public interface MutableAttributeInstance {

    // Returns the unique identifier of the attribute instance
    Identifier data_attributes$get_id();

    // Performs an action on the attribute modifier
    void data_attributes$actionModifier(final VoidConsumer consumerIn, final EntityAttributeInstance instanceIn, final EntityAttributeModifier modifierIn, final boolean isWasAdded);

    // Sets a callback for changes to the associated AttributeContainer
    void setContainerCallback(final AttributeContainer containerIn);

    // Updates the identifier of the attribute instance
    void updateId(final Identifier identifierIn);

    // Refreshes the attribute instance
    void refresh();
}
