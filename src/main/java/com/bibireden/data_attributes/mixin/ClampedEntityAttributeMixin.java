package com.bibireden.data_attributes.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bibireden.data_attributes.api.attribute.StackingFormula;

import net.minecraft.entity.attribute.ClampedEntityAttribute;

// Mixing in additional behavior to ClampedEntityAttribute
@Mixin(ClampedEntityAttribute.class)
abstract class ClampedEntityAttributeMixin extends EntityAttributeMixin {

	@Final
	@Shadow
	private double minValue; // The minimum value of the clamped attribute

	@Final
	@Shadow
	private double maxValue; // The maximum value of the clamped attribute

	// Injecting into the constructor to customize attribute behavior
	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_init(String translationKey, double fallback, double min, double max, CallbackInfo ci) {
		// Overriding the attribute with custom settings
		this.override(translationKey, min, max, fallback, 0.0D, StackingFormula.Flat);
	}

	// Injecting into the getMinValue method to return custom min value
	@Inject(method = "getMinValue", at = @At("HEAD"), cancellable = true)
	private void data_getMinValue(CallbackInfoReturnable<Double> ci) {
		ci.setReturnValue(this.minValue()); // Returning the custom min value
	}

	// Injecting into the getMaxValue method to return custom max value
	@Inject(method = "getMaxValue", at = @At("HEAD"), cancellable = true)
	private void data_getMaxValue(CallbackInfoReturnable<Double> ci) {
		ci.setReturnValue(this.maxValue()); // Returning the custom max value
	}

	// Injecting into the clamp method to customize attribute clamping
	@Inject(method = "clamp", at = @At("HEAD"), cancellable = true)
	private void data_clamp(double value, CallbackInfoReturnable<Double> ci) {
		ci.setReturnValue(this.data_clamped(value)); // Customizing clamping behavior
	}

	// Overriding the clear method to also clear custom values
	@Override
	public void clear() {
		super.clear(); // Calling the superclass clear method
		this.data_minValue = this.minValue; // Resetting custom min value
		this.data_maxValue = this.maxValue; // Resetting custom max value
	}
}
