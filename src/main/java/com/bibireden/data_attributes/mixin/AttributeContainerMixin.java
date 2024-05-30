package com.bibireden.data_attributes.mixin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
	private Map<Identifier, EntityAttributeInstance> data_custom = new HashMap<Identifier, EntityAttributeInstance>();

	@Unique
	private Map<Identifier, EntityAttributeInstance> data_tracked = new HashMap<Identifier, EntityAttributeInstance>();

	@Unique
	private LivingEntity data_livingEntity;

	@Final
	@Shadow
	private DefaultAttributeContainer fallback;

	@Shadow
	private void updateTrackedStatus(EntityAttributeInstance instance) {
	}

	// Injection to update the tracked status of the custom attributes
	@Inject(method = "updateTrackedStatus", at = @At("HEAD"), cancellable = true)
	private void data_updateTrackedStatus(EntityAttributeInstance instance, CallbackInfo ci) {
		Identifier identifier = ((MutableAttributeInstance) instance).getId();

		if (identifier != null) {
			this.data_tracked.put(identifier, instance);
		}

		ci.cancel();
	}

	// Injection to get the tracked custom attributes
	@Inject(method = "getTracked", at = @At("Head"), cancellable = true)
	private void data_getTracked(CallbackInfoReturnable<Set<EntityAttributeInstance>> cir) {
		Set<EntityAttributeInstance> tracked = this.data_tracked.values().stream().collect(Collectors.toSet());
		cir.setReturnValue(tracked);
	}

	// Redirecting the getAttributesToSend method to use custom attributes
	@Redirect(method = "getAttributesToSend", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_getAttributesToSend(Map<?, ?> instances) {
		return this.data_custom.values();
	}

	// Injection to get or create a custom attribute instance
	@Inject(method = "getCustomInstance", at = @At("HEAD"), cancellable = true)
	private void data_getCustomInstance(EntityAttribute attribute2,
			CallbackInfoReturnable<EntityAttributeInstance> ci) {
		Identifier identifier = Registries.ATTRIBUTE.getId(attribute2);

		if (identifier != null) {
			EntityAttributeInstance entityAttributeInstance = this.data_custom
					.computeIfAbsent(identifier,
							id -> this.fallback.createOverride(this::updateTrackedStatus, attribute2));

			if (entityAttributeInstance != null) {
				MutableAttributeInstance mutable = (MutableAttributeInstance) entityAttributeInstance;
				mutable.setContainerCallback((AttributeContainer) (Object) this);

				if (mutable.getId() == null) {
					mutable.updateId(identifier);
				}
			}

			ci.setReturnValue(entityAttributeInstance);
		} else {
			ci.setReturnValue((EntityAttributeInstance) null);
		}
	}

	// Redirecting methods to use custom attributes
	@Redirect(method = "hasAttribute", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_hasAttribute(Map<?, ?> instances, Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@Redirect(method = "hasModifierForAttribute", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_hasModifierForAttribute(Map<?, ?> instances, Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@Redirect(method = "getValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_getValue(Map<?, ?> instances, Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@Redirect(method = "getBaseValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_getBaseValue(Map<?, ?> instances, Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	@Redirect(method = "getModifierValue", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_getModifierValue(Map<?, ?> instances, Object attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId((EntityAttribute) attribute);
		return this.data_custom.get(identifier);
	}

	// Injection to remove custom modifiers
	@Inject(method = "removeModifiers", at = @At("HEAD"), cancellable = true)
	private void data_removeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers,
			CallbackInfo ci) {
		attributeModifiers.asMap().forEach((attribute, collection) -> {
			Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
			EntityAttributeInstance entityAttributeInstance = this.data_custom.get(identifier);

			if (entityAttributeInstance != null) {
				collection.forEach(entityAttributeInstance::removeModifier);
			}
		});

		ci.cancel();
	}

	// Injection to set custom attributes from another container
	@Inject(method = "setFrom", at = @At("HEAD"), cancellable = true)
	private void data_setFrom(AttributeContainer other, CallbackInfo ci) {
		AttributeContainer container = (AttributeContainer) (Object) this;

		((MutableAttributeContainer) other).custom().values().forEach(attributeInstance -> {
			EntityAttribute entityAttribute = attributeInstance.getAttribute();
			EntityAttributeInstance entityAttributeInstance = container.getCustomInstance(entityAttribute);

			if (entityAttributeInstance != null) {
				entityAttributeInstance.setFrom(attributeInstance);
				final double value = entityAttributeInstance.getValue();
				EntityAttributeModifiedEvents.MODIFIED.invoker().onModified(entityAttribute, this.data_livingEntity,
						null,
						value, false);
			}
		});

		ci.cancel();
	}

	// Redirecting to use custom attributes for serialization
	@Redirect(method = "toNbt", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"))
	private Collection<?> data_toNbt(Map<?, ?> instances) {
		return this.data_custom.values();
	}

	@Override
	public Map<Identifier, EntityAttributeInstance> custom() {
		return this.data_custom;
	}

	@Override
	public LivingEntity getLivingEntity() {
		return this.data_livingEntity;
	}

	@Override
	public void setLivingEntity(final LivingEntity livingEntity) {
		this.data_livingEntity = livingEntity;
	}

	@Override
	public void refresh() {
		for (EntityAttributeInstance instance : this.data_custom.values()) {
			((MutableAttributeInstance) instance).refresh();
		}
	}

	@Override
	public void clearTracked() {
		this.data_tracked.clear();
	}
}
