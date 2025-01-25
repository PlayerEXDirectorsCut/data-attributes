package com.bms.data_attributes.mutable;

import java.util.HashMap;
import java.util.Map;

import com.bms.data_attributes.api.attribute.IAttribute;
import com.bms.data_attributes.api.attribute.StackingFormula;
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride;
import com.bms.data_attributes.config.functions.AttributeFunction;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/**
 * Extends the {@link net.minecraft.world.entity.ai.attributes.Attribute} class
 * to implement child-parent relationships with other attributes,
 * allow overrides, and sum calculations of the attribute.
 */
public interface MutableAttribute extends IAttribute {
    /** Checks if one attribute contains another via the child-parent relationship. */
    static boolean contains(MutableAttribute lhs, MutableAttribute rhs) {
        if(rhs == null || lhs == rhs) return true;
        for(IAttribute conv : lhs.data_attributes$parentsMutable().keySet()) {
            MutableAttribute attribute = (MutableAttribute) conv;
            if(MutableAttribute.contains(attribute, rhs)) return true;
        }
        return false;
    }

    /** Overrides entity attribute properties. */
    default void data_attributes$override(AttributeOverride override) {}

    /** Adds a parent attribute with a function. */
    default void data_attributes$addParent(MutableAttribute attribute, final AttributeFunction function) {}

    /** Adds a child attribute with a function. */
    default void data_attributes$addChild(MutableAttribute attribute, final AttributeFunction function) {}

    /** Clears all properties and relationships of the entity attribute. */
    default void data_attributes$clear() {}

    /** Calculates the sum based on the {@link StackingFormula}.
     * By including a secondary pair of variables,
     * it allows for the modeling of more complex scenarios, such as negative modifiers.
     */
    default Double data_attributes$sum(final double k, final double k2, final double v, final double v2, AttributeInstance instance) { return null; }

    /** Returns a mutable map of parent entity attributes with associated functions. */
    default Map<IAttribute, AttributeFunction> data_attributes$parentsMutable() { return new HashMap<>(); }

    /** Returns a mutable map of child entity attributes with associated functions. */
    default Map<IAttribute, AttributeFunction> data_attributes$childrenMutable() { return new HashMap<>(); }
}
