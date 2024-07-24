package com.bibireden.data_attributes.mixin;

import java.util.*;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.mutable.MutableAttributeContainer;
import com.bibireden.data_attributes.mutable.MutableAttributeInstance;
import com.google.common.collect.Multimap;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

@Mixin(AttributeContainer.class)
abstract class AttributeContainerMixin implements MutableAttributeContainer {
	@Unique
	private final Map<Identifier, EntityAttributeInstance> data_attributes$custom = new HashMap<>();

	@Unique
	private final Map<Identifier, EntityAttributeInstance> data_attributes$tracked = new HashMap<>();

	@Unique
	private LivingEntity data_attributes$livingEntity;

	@Final
	@Shadow
	private DefaultAttributeContainer fallback;

	@Shadow
	private void updateTrackedStatus(EntityAttributeInstance instance) {}

	@Shadow @Nullable public abstract EntityAttributeInstance getCustomInstance(EntityAttribute attribute);

	@Inject(method = "updateTrackedStatus", at = @At("HEAD"), cancellable = true)
	private void data_attributes$updateTrackedStatus(EntityAttributeInstance instance, CallbackInfo ci) {
		Identifier identifier = ((MutableAttributeInstance) instance).data_attributes$get_id();
		if (identifier != null) {
			this.data_attributes$tracked.put(identifier, instance);
		}
		ci.cancel();
	}

	@ModifyReturnValue(method = "getTracked", at = @At("RETURN"))
	private Set<EntityAttributeInstance> data_attributes$getTracked(Set<EntityAttributeInstance> original) {
		return new HashSet<>(this.data_attributes$tracked.values());
	}

	@ModifyReceiver(method = "getAttributesToSend", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Map<?, ?> data_attributes$getAttributesToSend(Map<?, ?> instances) { return this.data_attributes$custom; }

	@Nullable
	@ModifyReturnValue(method = "getCustomInstance(Lnet/minecraft/entity/attribute/EntityAttribute;)Lnet/minecraft/entity/attribute/EntityAttributeInstance;", at = @At("RETURN"))
	private EntityAttributeInstance data_attributes$getCustomInstance(EntityAttributeInstance original, EntityAttribute attribute2) {
		Identifier identifier = Registries.ATTRIBUTE.getId(attribute2);
		if (identifier == null) return original;

		EntityAttributeInstance entityAttributeInstance = this.data_attributes$custom.computeIfAbsent(identifier, id -> this.fallback.createOverride(this::updateTrackedStatus, attribute2));
		if (entityAttributeInstance != null) {
			MutableAttributeInstance mutable = (MutableAttributeInstance) entityAttributeInstance;
			mutable.data_attributes$setContainerCallback((AttributeContainer) (Object) this);
			if (mutable.data_attributes$get_id() == null) {
				mutable.data_attributes$updateId(identifier);
			}
		}

		return entityAttributeInstance;
	}

	@ModifyReturnValue(method = "hasAttribute(Lnet/minecraft/entity/attribute/EntityAttribute;)Z", at = @At("RETURN"))
	private boolean data_attributes$hasAttribute(boolean original, EntityAttribute attribute) {
		var identifier = Registries.ATTRIBUTE.getId(attribute);
		return this.data_attributes$custom.get(identifier) != null || original;
	}

	@WrapOperation(
		method = "hasModifierForAttribute(Lnet/minecraft/entity/attribute/EntityAttribute;Ljava/util/UUID;)Z",
		at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
	)
	private <V> Object data_attributes$hasModifierForAttribute(Map<?, ?> instances, Object attribute, Operation<V> original) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		var value = this.data_attributes$custom.get(identifier);
		return value != null ? value : original.call(instances, attribute);
	}

	@ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getValue(Object original, @Local(argsOnly = true) EntityAttribute attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getBaseValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getBaseValue(Object attribute, @Local(argsOnly = true) EntityAttribute param) {
		Identifier identifier = Registries.ATTRIBUTE.getId(param);
		return this.data_attributes$custom.get(identifier);
	}

	@ModifyExpressionValue(method = "getModifierValue(Lnet/minecraft/entity/attribute/EntityAttribute;Ljava/util/UUID;)D", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$getModifierValue(Object attribute, @Local(argsOnly = true) EntityAttribute param) {
		Identifier identifier = Registries.ATTRIBUTE.getId(param);
		return this.data_attributes$custom.get(identifier);
	}

	@Inject(method = "removeModifiers", at = @At("HEAD"), cancellable = true)
	private void data_attributes$removeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers, CallbackInfo ci) {
		attributeModifiers.asMap().forEach((attribute, collection) -> {
			Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
			EntityAttributeInstance entityAttributeInstance = this.data_attributes$custom.get(identifier);

			if (entityAttributeInstance != null) {
				collection.forEach(entityAttributeInstance::removeModifier);
			}
		});

		ci.cancel();
	}

	@Inject(method = "setFrom", at = @At("HEAD"), cancellable = true)
	private void data_attributes$setFrom(AttributeContainer other, CallbackInfo ci) {
		((MutableAttributeContainer) other).data_attributes$custom().values().forEach(attributeInstance -> {
			EntityAttribute entityAttribute = attributeInstance.getAttribute();
			EntityAttributeInstance entityAttributeInstance = this.getCustomInstance(entityAttribute);

			if (entityAttributeInstance != null) {
				entityAttributeInstance.setFrom(attributeInstance);
				EntityAttributeModifiedEvents.MODIFIED.invoker().onModified(
					entityAttribute,
					this.data_attributes$livingEntity,
					null,
					entityAttributeInstance.getValue(),
					false
				);
			}
		});

		ci.cancel();
	}

	@ModifyExpressionValue(method = "toNbt", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_attributes$toNbt(Collection<?> original) {
		return this.data_attributes$custom.values();
	}

	@Override
	public Map<Identifier, EntityAttributeInstance> data_attributes$custom() {
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
		for (EntityAttributeInstance instance : this.data_attributes$custom.values()) {
			((MutableAttributeInstance) instance).data_attributes$refresh();
		}
	}

	@Override
	public void data_attributes$clearTracked() {
		this.data_attributes$tracked.clear();
	}
}
