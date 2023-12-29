package com.github.clevernucleus.dataattributes.mutable;

import com.github.clevernucleus.dataattributes.api.util.VoidConsumer;

import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public interface MutableAttributeInstance {

    // Returns the unique identifier of the attribute instance
    Identifier getId();

    // Performs an action on the attribute modifier
    void actionModifier(final VoidConsumer consumerIn, final EntityAttributeInstance instanceIn, final EntityAttributeModifier modifierIn, final boolean isWasAdded);

    // Sets a callback for changes to the associated AttributeContainer
    void setContainerCallback(final AttributeContainer containerIn);

    // Updates the identifier of the attribute instance
    void updateId(final Identifier identifierIn);

    // Refreshes the attribute instance
    void refresh();
}
