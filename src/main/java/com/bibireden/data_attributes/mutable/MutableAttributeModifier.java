package com.bibireden.data_attributes.mutable;

public interface MutableAttributeModifier {

    /** Updates the value of the modifier. */
    default void data_attributes$updateValue(double value) {}
}
