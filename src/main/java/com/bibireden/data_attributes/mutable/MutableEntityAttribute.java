package com.bibireden.data_attributes.mutable;

import java.util.HashMap;
import java.util.Map;

import com.bibireden.data_attributes.api.attribute.IEntityAttribute;
import com.bibireden.data_attributes.api.attribute.StackingFormula;
import com.bibireden.data_attributes.config.models.OverridesConfigModel.AttributeOverride;
import com.bibireden.data_attributes.config.functions.AttributeFunction;
import net.minecraft.entity.attribute.EntityAttributeInstance;

/**
 * Extends the {@link net.minecraft.entity.attribute.EntityAttribute} class
 * to implement child-parent relationships with other attributes,
 * allow overrides, and sum calculations of the attribute.
 */
public interface MutableEntityAttribute extends IEntityAttribute {
    /** Checks if one attribute contains another via the child-parent relationship. */
    static boolean contains(MutableEntityAttribute lhs, MutableEntityAttribute rhs) {
        if(rhs == null || lhs == rhs) return true;
        for(IEntityAttribute conv : lhs.data_attributes$parentsMutable().keySet()) {
            MutableEntityAttribute attribute = (MutableEntityAttribute) conv;
            if(MutableEntityAttribute.contains(attribute, rhs)) return true;
        }
        return false;
    }

    /** Overrides entity attribute properties. */
    default void data_attributes$override(AttributeOverride override) {}

    /** Adds a parent attribute with a function. */
    default void data_attributes$addParent(MutableEntityAttribute attribute, final AttributeFunction function) {}

    /** Adds a child attribute with a function. */
    default void data_attributes$addChild(MutableEntityAttribute attribute, final AttributeFunction function) {}

    /** Clears all properties and relationships of the entity attribute. */
    default void data_attributes$clear() {}

    /** Calculates the sum based on the {@link StackingFormula}.
     * By including a secondary pair of variables,
     * it allows for the modeling of more complex scenarios, such as negative modifiers.
     */
    default Double data_attributes$sum(final double k, final double k2, final double v, final double v2, EntityAttributeInstance instance) { return null; }

    /** Returns a mutable map of parent entity attributes with associated functions. */
    default Map<IEntityAttribute, AttributeFunction> data_attributes$parentsMutable() { return new HashMap<>(); }

    /** Returns a mutable map of child entity attributes with associated functions. */
    default Map<IEntityAttribute, AttributeFunction> data_attributes$childrenMutable() { return new HashMap<>(); }
}
