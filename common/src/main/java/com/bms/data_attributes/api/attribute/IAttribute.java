package com.bms.data_attributes.api.attribute;

import com.bms.data_attributes.config.functions.AttributeFunction;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Attribute's implement this through a mixin, and can be cast to this interface to get additional data added by Data Attributes.
 * 
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 *
 */
public interface IAttribute {
	/**
	 * @return The minimum value of the attribute.
	 */
	default Double data_attributes$min() { return null; }
	
	/**
	 * @return The maximum value of the attribute.
	 */
	default Double data_attributes$max() { return null; }

	/** Returns the intended minimum fallback of this attribute. */
	default Double data_attributes$min_fallback() { return null; }

	/** Returns the intended maximum fallback of this attribute. */
	default Double data_attributes$max_fallback() { return null; }

	/** Returns the smoothness of this attribute. */
	default Double data_attributes$smoothness() { return null; }
	
	/**
	 * @return The attribute's {@link StackingFormula}.
	 */
	default StackingFormula data_attributes$formula() { return null; }

	default AttributeFormat data_attributes$format() { return null; }
	/**
	 * @return An immutable map of the function-parents attached to this attribute.
	 * @since 1.4.0
	 */
	default Map<IAttribute, AttributeFunction> data_attributes$parents() { return null; }
	
	/**
	 * @return An immutable map of the function-children attached to this attribute.
	 * @since 1.4.0
	 */
	default Map<IAttribute, AttributeFunction> data_attributes$children() { return null; }
}
