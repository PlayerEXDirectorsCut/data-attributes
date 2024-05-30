package com.bibireden.data_attributes.mutable;

import java.util.Map;

import com.bibireden.data_attributes.api.attribute.IEntityAttribute;
import com.bibireden.data_attributes.api.attribute.StackingFormula;
import com.bibireden.data_attributes.json.AttributeFunctionJson;

public interface MutableEntityAttribute extends IEntityAttribute {

    // Overrides the properties of the entity attribute
    void override(String translationKey, double minValue, double maxValue, double fallbackValue, double incrementValue, StackingFormula stackingFormula);

    // Adds a parent entity attribute with an associated function
    void addParent(MutableEntityAttribute attributeIn, final AttributeFunctionJson function);

    // Adds a child entity attribute with an associated function
    void addChild(MutableEntityAttribute attributeIn, final AttributeFunctionJson function);

    // Clears all properties and relationships of the entity attribute
    void clear();

    // Calculates the sum of values using specific parameters
    double sum(final double k, final double k2, final double v, final double v2);

    // Checks if a given entity attribute is contained within another
    boolean contains(MutableEntityAttribute a, MutableEntityAttribute b);

    // Returns a mutable map of parent entity attributes with associated functions
    Map<IEntityAttribute, AttributeFunctionJson> parentsMutable();

    // Returns a mutable map of child entity attributes with associated functions
    Map<IEntityAttribute, AttributeFunctionJson> childrenMutable();
}
