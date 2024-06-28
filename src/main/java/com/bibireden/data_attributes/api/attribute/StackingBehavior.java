package com.bibireden.data_attributes.api.attribute;

/**
 * @since 1.4.0
 * @author CleverNucleus
 */
public enum StackingBehavior {
	/** Addition of values as defined by the parent attribute. Equivalent of EntityAttributeModifier.Operation.ADDITION. */
	Add((byte)0),
	/** Multiplication of parent attribute. Equivalent of EntityAttributeModifier.Operation.MULTIPLY_TOTAL. */
	Multiply((byte)1);
	
	private final byte id;
	
	StackingBehavior(final byte id) {
		this.id = id;
	}
	
	public static StackingBehavior of(final byte id) {
		return switch(id) {
			case 0 -> Add;
			case 1 -> Multiply;
			default -> Add;
		};
	}

	public static StackingBehavior of(final String id) {
		return id.equalsIgnoreCase("multiply") ? Multiply : Add;
	}
	
	public byte id() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return String.valueOf(this.id);
	}
}
