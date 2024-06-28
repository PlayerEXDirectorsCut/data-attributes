package com.bibireden.data_attributes.api.attribute;

/**
 * @since 1.4.0
 * @author CleverNucleus
 */
public interface IAttributeFunction {
	
	/**
	 * @return The FunctionBehaviour associated with this attribute function.
	 */
	StackingBehavior behavior();
	
	/**
	 * @return The value associated with this attribute function.
	 */
	double value();
}
