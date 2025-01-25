package com.bms.data_attributes.mixin;

import java.util.HashMap;
import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.mutable.MutableAttributeSupplier;

/** Swaps out the default instances map for a custom one that has an {@link ResourceLocation} as a key instead. */
@Mixin(AttributeSupplier.class)
abstract class AttributeSupplierMixin implements MutableAttributeSupplier {
	@Unique
	private Map<ResourceLocation, AttributeInstance> data_attributes$instances;

	@Final
	@Shadow
	private Map<Holder<Attribute>, AttributeInstance> instances;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(Map<Holder<Attribute>, AttributeInstance> instances, CallbackInfo ci) {
		this.data_attributes$instances = new HashMap<>();
		instances.forEach((holder, instance) -> {
			ResourceLocation key = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
			if (key != null) {
				this.data_attributes$instances.put(key, instance);
			}
		});
	}

	@ModifyExpressionValue(method = "getAttributeInstance", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$require(Object original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$instances.getOrDefault(identifier, (AttributeInstance) original);
	}

	@ModifyExpressionValue(method = "createInstance", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$createOverride(Object original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$instances.getOrDefault(identifier, (AttributeInstance) original);
	}

	@ModifyReturnValue(method = "hasAttribute", at = @At("RETURN"))
	private boolean data_attributes$has(boolean original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$instances.containsKey(identifier) || original;
	}

	@ModifyReturnValue(method = "hasModifier", at = @At("RETURN"))
	private boolean data_attributes$hasModifier(boolean original, @Local(argsOnly = true) Holder<Attribute> holder, @Local(argsOnly = true) ResourceLocation id) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		var instance = this.data_attributes$instances.get(identifier);
		return (instance != null && instance.getModifier(id) != null) || original;
	}

	@Override
	public void data_attributes$copy(AttributeSupplier.Builder builder) {
		this.instances.forEach((Attribute, instance) -> builder.add(Attribute, instance.getBaseValue()));
	}
}
