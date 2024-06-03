package com.bibireden.data_attributes.mutable;

import java.util.Map;

import com.bibireden.data_attributes.api.attribute.IEntityAttribute;
import com.bibireden.data_attributes.data.AttributeFunction;
import com.bibireden.data_attributes.data.AttributeOverride;

public interface MutableEntityAttribute extends IEntityAttribute {

    // Overrides the properties of the entity attribute
    void data_attributes$override(AttributeOverride override);

    // Adds a parent entity attribute with an associated function
    void data_attributes$addParent(MutableEntityAttribute attributeIn, final AttributeFunction function);

    // Adds a child entity attribute with an associated function
    void data_attributes$addChild(MutableEntityAttribute attributeIn, final AttributeFunction function);

    // Clears all properties and relationships of the entity attribute
    void data_attributes$clear();

    // Calculates the sum of values using specific parameters
    double data_attributes$sum(final double k, final double k2, final double v, final double v2);

    // Checks if a given entity attribute is contained within another
    boolean data_attributes$contains(MutableEntityAttribute a, MutableEntityAttribute b);

    // Returns a mutable map of parent entity attributes with associated functions
    Map<IEntityAttribute, AttributeFunction> data_attributes$parentsMutable();

    // Returns a mutable map of child entity attributes with associated functions
    Map<IEntityAttribute, AttributeFunction> data_attributes$childrenMutable();
}
