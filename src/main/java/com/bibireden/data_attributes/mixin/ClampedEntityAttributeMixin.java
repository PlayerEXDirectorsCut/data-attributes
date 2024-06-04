package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.data.AttributeOverride;
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
		this.data_attributes$override(new AttributeOverride(fallback, min, max, 0.0D, StackingFormula.Flat, translationKey));
	}

	@ModifyReturnValue(method = "getMinValue", at = @At("RETURN"))
	private double data_attributes$getMin(double original) {
		return this.data_attributes$min();
	}

	@ModifyReturnValue(method = "getMaxValue", at = @At("RETURN"))
	private double data_attributes$getMax(double original) {
		return this.data_attributes$max();
	}

	@ModifyReturnValue(method = "clamp", at = @At("RETURN"))
	private double data_attributes$clamp(double original) {
		return this.data_attributes$clamped(original);
	}

	@Override
	public void data_attributes$clear() {
		super.data_attributes$clear();
		this.data_min = this.minValue;
		this.data_max = this.maxValue;
	}
}
