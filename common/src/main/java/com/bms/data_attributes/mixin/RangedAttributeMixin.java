package com.bms.data_attributes.mixin;

import com.bms.data_attributes.api.attribute.AttributeFormat;
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.api.attribute.StackingFormula;

@Mixin(RangedAttribute.class)
abstract class RangedAttributeMixin extends AttributeMixin {
	@Final
	@Shadow
	private double minValue;

	@Final
	@Shadow
	private double maxValue;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(String translationKey, double fallback, double min, double max, CallbackInfo ci) {
		this.data_attributes$override(new AttributeOverride(false, minValue, maxValue, min, max, 0.0, StackingFormula.Flat, AttributeFormat.Whole));
	}

	@ModifyReturnValue(method = "getMinValue", at = @At("RETURN"))
	private double data_attributes$getMin(double original) {
		return this.data_attributes$enabled ? this.data_attributes$min() : original;
	}

	@ModifyReturnValue(method = "getMaxValue", at = @At("RETURN"))
	private double data_attributes$getMax(double original) {
		return this.data_attributes$enabled ? this.data_attributes$max() : original;
	}

	@ModifyReturnValue(method = "sanitizeValue", at = @At("RETURN"))
	private double data_attributes$clamp(double original, @Local(argsOnly = true) double value) {
		return this.data_attributes$enabled ? this.data_attributes$clamped(value) : original;
	}

	@Override
	public Double data_attributes$smoothness() { return this.data_attributes$smoothness; }

	@Override
	public Double data_attributes$min_fallback() {
		return this.minValue;
	}

	@Override
	public Double data_attributes$max_fallback() { return this.maxValue; }

	@Override
	public void data_attributes$clear() {
		super.data_attributes$clear();
		this.data_attributes$min = this.minValue;
		this.data_attributes$max = this.maxValue;
	}
}
