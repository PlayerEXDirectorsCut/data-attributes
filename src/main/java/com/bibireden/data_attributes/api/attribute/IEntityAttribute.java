package com.bibireden.data_attributes.api.attribute;

import com.bibireden.data_attributes.config.functions.AttributeFunction;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Map;

/**
 * 
 * EntityAttribute's implement this through a mixin, and can be cast to this interface to get additional data added by Data Attributes.
 * 
 * @author Bare Minimum Studios (B.M.S), CleverNucleus
 *
 */
public interface IEntityAttribute {
	/** Gets the {@link RegistryEntry} that is associated with this attribute. */
	RegistryEntry<EntityAttribute> data_attributes$entry();

	/**
	 * @return The minimum value of the attribute.
	 */
	double data_attributes$min();
	
	/**
	 * @return The maximum value of the attribute.
	 */
	double data_attributes$max();

	/** Returns the intended minimum fallback of this attribute. */
	double data_attributes$min_fallback();

	/** Returns the intended maximum fallback of this attribute. */
	double data_attributes$max_fallback();

    /** Returns the smoothness of this attribute. */
	double data_attributes$smoothness();
	
	/**
	 * @return The attribute's {@link StackingFormula}.
	 */
	StackingFormula data_attributes$formula();
	
	/**
	 * @return An immutable map of the function-parents attached to this attribute.
	 * @since 1.4.0
	 */
	Map<IEntityAttribute, AttributeFunction> data_attributes$parents();
	
	/**
	 * @return An immutable map of the function-children attached to this attribute.
	 * @since 1.4.0
	 */
	Map<IEntityAttribute, AttributeFunction> data_attributes$children();
}
