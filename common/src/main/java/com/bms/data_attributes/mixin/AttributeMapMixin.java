package com.bms.data_attributes.mixin;

import java.util.*;

import com.bms.data_attributes.api.event.AttributeModifiedEvents;
import com.bms.data_attributes.mutable.MutableAttributeInstance;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.mutable.MutableAttributeMap;

@Mixin(AttributeMap.class)
abstract class AttributeMapMixin implements MutableAttributeMap {
	@Unique
	private final Map<ResourceLocation, AttributeInstance> data_attributes$custom = new HashMap<>();

	@Unique
	private final Map<ResourceLocation, AttributeInstance> data_attributes$tracked = new HashMap<>();

	@Unique
	private LivingEntity data_attributes$livingEntity;

	@Final
	@Shadow
	private AttributeSupplier supplier;

	@Shadow
	private void onAttributeModified(AttributeInstance instance) {}

	@Shadow @Nullable public abstract AttributeInstance getInstance(Holder<Attribute> attribute);

	@Inject(method = "onAttributeModified", at = @At("HEAD"), cancellable = true)
	private void data_attributes$onAttributeModified(AttributeInstance instance, CallbackInfo ci) {
		ResourceLocation identifier = ((MutableAttributeInstance) instance).data_attributes$get_id();
		if (identifier != null) {
			this.data_attributes$tracked.put(identifier, instance);
		}
		ci.cancel();
	}

	@ModifyReturnValue(method = "getAttributesToSync", at = @At("RETURN"))
	private Set<AttributeInstance> data_attributes$getTracked(Set<AttributeInstance> original) {
		return new HashSet<>(this.data_attributes$tracked.values());
	}

	@ModifyReceiver(method = "getSyncableAttributes", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Map<?, ?> data_attributes$getAttributesToSend(Map<?, ?> instances) { return this.data_attributes$custom; }

	@Nullable
	@ModifyReturnValue(method = "getInstance", at = @At("RETURN"))
	private AttributeInstance data_attributes$getInstance(AttributeInstance original, Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		if (identifier == null) return original;

		MutableAttributeInstance instance = (MutableAttributeInstance) this.data_attributes$custom.computeIfAbsent(identifier, id -> this.supplier.createInstance(this::onAttributeModified, holder));
		if (instance != null) {
			instance.data_attributes$setContainerCallback((AttributeMap) (Object) this);
			if (instance.data_attributes$get_id() == null) {
				instance.data_attributes$updateId(identifier);
			}
		}

		return (AttributeInstance) instance;
	}

	@ModifyReturnValue(method = "hasAttribute", at = @At("RETURN"))
	private boolean data_attributes$hasAttribute(boolean original, Holder<Attribute> attribute) {
		var identifier = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
		return this.data_attributes$custom.get(identifier) != null || original;
	}

	@WrapOperation(
		method = "hasModifier",
		at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private <V> Object data_attributes$hasModifier(Map<?, ?> instances, Object attribute, Operation<V> original) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey((Attribute) attribute);
		var value = this.data_attributes$custom.get(identifier);
		return value != null ? value : original.call(instances, attribute);
	}

	@ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getValue(Object original, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getBaseValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getBaseValue(Object attribute, @Local(argsOnly = true) Holder<Attribute> holder) {
		ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getModifierValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getModifierValue(Object attribute, @Local(argsOnly = true) Holder<Attribute> holder) {
		return this.data_attributes$custom.get(BuiltInRegistries.ATTRIBUTE.getKey(holder.value()));
	}

	@Inject(method = "removeAttributeModifiers", at = @At("HEAD"), cancellable = true)
	private void data_attributes$removeModifiers(Multimap<Holder<Attribute>, AttributeModifier> modifiers, CallbackInfo ci) {
		modifiers.asMap().forEach((holder, collection) -> {
			ResourceLocation identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
			AttributeInstance AttributeInstance = this.data_attributes$custom.get(identifier);

			if (AttributeInstance != null) {
				collection.forEach(AttributeInstance::removeModifier);
			}
		});

		ci.cancel();
	}

	@Inject(method = "assignAllValues", at = @At("HEAD"), cancellable = true)
	private void data_attributes$setFrom(AttributeMap other, CallbackInfo ci) {
		MutableAttributeMap mutableMap = (MutableAttributeMap) other;
		mutableMap.data_attributes$custom().values().forEach(attributeInstance -> {
			Holder<Attribute> holder = attributeInstance.getAttribute();
			AttributeInstance AttributeInstance = this.getInstance(holder);

			if (AttributeInstance != null) {
				AttributeInstance.replaceFrom(attributeInstance);
				AttributeModifiedEvents.MODIFIED.invoker().onModified(
					holder.value(),
					this.data_attributes$livingEntity,
					null,
					AttributeInstance.getValue(),
					false
				);
			}
		});

		ci.cancel();
	}

	@ModifyExpressionValue(method = "save", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_attributes$save(Collection<?> original) {
		return this.data_attributes$custom.values();
	}

	@Override
	public Map<ResourceLocation, AttributeInstance> data_attributes$custom() {
		return this.data_attributes$custom;
	}

	@Override
	public LivingEntity data_attributes$getLivingEntity() {
		return this.data_attributes$livingEntity;
	}

	@Override
	public void data_attributes$setLivingEntity(final LivingEntity livingEntity) {
		this.data_attributes$livingEntity = livingEntity;
	}

	@Override
	public void data_attributes$refresh() {
		this.data_attributes$custom.values().forEach((instance) -> ((MutableAttributeInstance) instance).data_attributes$refresh());
	}

	@Override
	public void data_attributes$clearTracked() {
		this.data_attributes$tracked.clear();
	}
}
