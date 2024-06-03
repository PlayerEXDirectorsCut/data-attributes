package com.bibireden.data_attributes.api.attribute;

import java.util.Map;

/**
 * 
 * EntityAttribute's implement this through a mixin, and can be cast to this interface to get additional data added by Data Attributes.
 * 
 * @author CleverNucleus
 *
 */
public interface IEntityAttribute {
	/**
	 * @return The attribute's minimum value;
	 */
	double data_attributes$min();
	
	/**
	 * @return The attribute's maximum value;
	 */
	double data_attributes$max();
	
	/**
	 * @return The attribute's stacking behaviour.
	 */
	StackingFormula data_attributes$formula();
	
	/**
	 * @return An immutable map of the function-parents attached to this attribute.
	 * @since 1.4.0
	 */
	Map<IEntityAttribute, IAttributeFunction> data_attributes$parents();
	
	/**
	 * @return An immutable map of the function-children attached to this attribute.
	 * @since 1.4.0
	 */
	Map<IEntityAttribute, IAttributeFunction> data_attributes$children();
}
