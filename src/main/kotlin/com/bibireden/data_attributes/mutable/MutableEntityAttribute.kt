package com.bibireden.data_attributes.mutable

import com.bibireden.data_attributes.api.attribute.DynamicEntityAttribute
import com.bibireden.data_attributes.registry.AttributeSkillData
import com.bibireden.data_attributes.registry.AttributeOverrideData

interface MutableEntityAttribute : DynamicEntityAttribute {
    // Overrides the properties of the entity attribute
    fun override(override: AttributeOverrideData)

    // Adds a parent entity attribute with an associated function
    fun addParent(attributeIn: MutableEntityAttribute, function: AttributeSkillData)

    // Adds a child entity attribute with an associated function
    fun addChild(attributeIn: MutableEntityAttribute, function: AttributeSkillData)

    // Clears all properties and relationships of the entity attribute
    fun clear()

    // Checks if a given entity attribute is contained within another
    fun contains(a: MutableEntityAttribute, b: MutableEntityAttribute): Boolean

    // Returns a mutable map of parent entity attributes with associated functions
    fun parentsMutable(): Map<DynamicEntityAttribute, AttributeSkillData>

    // Returns a mutable map of child entity attributes with associated functions
    fun childrenMutable(): Map<DynamicEntityAttribute, AttributeSkillData>
}