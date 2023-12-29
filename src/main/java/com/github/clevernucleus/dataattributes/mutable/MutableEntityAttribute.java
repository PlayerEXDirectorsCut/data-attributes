package com.github.clevernucleus.dataattributes.mutable;

import java.util.Map;

import com.github.clevernucleus.dataattributes.api.attribute.IEntityAttribute;
import com.github.clevernucleus.dataattributes.api.attribute.StackingBehaviour;
import com.github.clevernucleus.dataattributes.json.AttributeFunctionJson;

public interface MutableEntityAttribute extends IEntityAttribute {

    // Overrides the properties of the entity attribute
    void override(String translationKey, double minValue, double maxValue, double fallbackValue, double incrementValue, StackingBehaviour stackingBehaviour);

    // Sets additional properties for the entity attribute
    void properties(Map<String, String> properties);

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
