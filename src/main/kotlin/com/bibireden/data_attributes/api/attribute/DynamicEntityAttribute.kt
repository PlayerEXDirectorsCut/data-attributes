package com.bibireden.data_attributes.api.attribute

import com.bibireden.data_attributes.api.enums.FunctionBehavior
import com.bibireden.data_attributes.api.enums.StackingFormula

/**
 * `EntityAttributes` will implement this through mixins, and are able to be casted using this interface to get additional data provided by DataAttributes.
 */
interface DynamicEntityAttribute {
    /**
     * @return The attribute's minimum value;
     */
    fun minValue(): Double

    /**
     * @return The attribute's maximum value;
     */
    fun maxValue(): Double

    /** Returns: The attribute's function behavior. */
    fun behavior(): FunctionBehavior

    /**
     * @return The attribute's stacking behaviour.
     */
    fun formula(): StackingFormula

    /**
     * @return An immutable map of the function-parents attached to this attribute.
     * @since 1.4.0
     */
    fun parents(): Map<DynamicEntityAttribute, DynamicSkillAttribute>

    /**
     * @return An immutable map of the function-children attached to this attribute.
     * @since 1.4.0
     */
    fun children(): Map<DynamicEntityAttribute, DynamicSkillAttribute>
}