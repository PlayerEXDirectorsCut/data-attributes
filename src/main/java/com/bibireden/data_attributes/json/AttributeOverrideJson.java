package com.bibireden.data_attributes.json;

import com.bibireden.data_attributes.api.attribute.StackingFormula;
import com.bibireden.data_attributes.data.AttributeOverride;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;
import com.google.gson.annotations.Expose;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;

public final class AttributeOverrideJson {
	@Expose private double fallback;
	@Expose private double min;
	@Expose private double max;
	@Expose private double increment;
	@Expose private String translationKey;
	@Expose private StackingFormula formula;
	
	public AttributeOverrideJson() {}
	
	public EntityAttribute create() {
		return new ClampedEntityAttribute(this.translationKey, this.fallback, this.min, this.max);
	}
	
	public void override(final MutableEntityAttribute entityAttribute) {
		entityAttribute.override(new AttributeOverride(this.min, this.max, this.fallback, this.increment, this.formula, this.translationKey));
	}
	
	public void readFromNbt(NbtCompound tag) {
		this.fallback = tag.getDouble("FallbackValue");
		this.min = tag.getDouble("MinValue");
		this.max = tag.getDouble("MaxValue");
		this.increment = tag.getDouble("IncrementValue");
		this.translationKey = tag.getString("TranslationKey");
		byte formula = tag.getByte("StackingBehaviour");
		this.formula = StackingFormula.of(formula);
	}
	
	public void writeToNbt(NbtCompound tag) {
		tag.putDouble("FallbackValue", this.fallback);
		tag.putDouble("MinValue", this.min);
		tag.putDouble("MaxValue", this.max);
		tag.putDouble("IncrementValue", this.increment);
		tag.putString("TranslationKey", this.translationKey);
		tag.putByte("StackingBehaviour", this.formula.id());
	}
}
