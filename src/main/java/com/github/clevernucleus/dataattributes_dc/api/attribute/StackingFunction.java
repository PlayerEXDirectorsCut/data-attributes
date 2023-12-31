package com.github.clevernucleus.dataattributes_dc.api.attribute;

/**
 * A functional interface used by stacking behaviour - essentially just a five input function.
 * 
 * @author CleverNucleus
 *
 */
@FunctionalInterface
public interface StackingFunction {
	double result(final double k, final double k2, final double v, final double v2, final double i);
}
