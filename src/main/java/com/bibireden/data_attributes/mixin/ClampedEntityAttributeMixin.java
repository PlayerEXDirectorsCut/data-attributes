package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.config.OverridesConfigModel;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.api.attribute.StackingFormula;

import net.minecraft.entity.attribute.ClampedEntityAttribute;

@Mixin(ClampedEntityAttribute.class)
abstract class ClampedEntityAttributeMixin extends EntityAttributeMixin {
	@Final
	@Shadow
	private double minValue;

	@Final
	@Shadow
	private double maxValue;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(String translationKey, double fallback, double min, double max, CallbackInfo ci) {
		this.data_attributes$override(new OverridesConfigModel.AttributeOverrideConfig(false, minValue, maxValue, min, max, 0.0D, StackingFormula.Flat));
	}

	@ModifyReturnValue(method = "getMinValue", at = @At("RETURN"))
	private double data_attributes$getMin(double original) {
		if (this.data_enabled) {
			return this.data_attributes$min();
		}

		return original;
	}

	@ModifyReturnValue(method = "getMaxValue", at = @At("RETURN"))
	private double data_attributes$getMax(double original) {
		if (this.data_enabled) {
			return this.data_attributes$max();
		}

		return original;
	}

	@ModifyReturnValue(method = "clamp", at = @At("RETURN"))
	private double data_attributes$clamp(double original) {
		if (this.data_enabled) {
			return this.data_attributes$clamped(original);
		}

		return original;
	}

	@Override
	public void data_attributes$clear() {
		super.data_attributes$clear();
		this.data_min = this.minValue;
		this.data_max = this.maxValue;
	}
}
