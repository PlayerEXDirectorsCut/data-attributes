package com.bibireden.data_attributes.json;

import com.bibireden.data_attributes.api.attribute.StackingBehavior;
import com.bibireden.data_attributes.api.attribute.IAttributeFunction;
import com.bibireden.data_attributes.api.util.Maths;
import com.google.gson.annotations.Expose;

public final class AttributeFunctionJson implements IAttributeFunction {
	@Expose private StackingBehavior behaviour;
	@Expose private double value;
	
	private AttributeFunctionJson() {}
	
	public static AttributeFunctionJson read(byte[] byteArray) {
		AttributeFunctionJson functionTypeJson = new AttributeFunctionJson();
		functionTypeJson.behaviour = StackingBehavior.of(byteArray[8]);
		functionTypeJson.value = Maths.byteArrayToDouble(byteArray);
		return functionTypeJson;
	}
	
	public byte[] write() {
		byte[] byteArray = Maths.doubleToByteArray(this.value, 9);
		byteArray[8] = this.behaviour.id();
		return byteArray;
	}
	
	@Override
	public StackingBehavior behaviour() {
		return this.behaviour;
	}
	
	@Override
	public double value() {
		return this.value;
	}
}
