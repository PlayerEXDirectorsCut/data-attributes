package com.bibireden.data_attributes.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

@Mixin(DefaultAttributeContainer.class)
abstract class DefaultAttributeContainerMixin implements MutableDefaultAttributeContainer {
	@Unique
	private Map<Identifier, EntityAttributeInstance> data_instances; // Custom map for attribute instances

	@Final
	@Shadow
	private Map<EntityAttribute, EntityAttributeInstance> instances; // Original map of attribute instances

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_init(Map<EntityAttribute, EntityAttributeInstance> instances, CallbackInfo ci) {
		this.data_instances = new HashMap<Identifier, EntityAttributeInstance>();

		// Populating the custom map with identifiers and instances
		instances.forEach((attribute, instance) -> {
			Identifier key = Registries.ATTRIBUTE.getId(attribute);

			if (key != null) {
				this.data_instances.put(key, instance);
			}
		});
	}

	// todo: needs verification via debug that this works as intended.
	@ModifyExpressionValue(method = "require", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_require(Object original, EntityAttribute attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
		return this.data_instances.getOrDefault(identifier, (EntityAttributeInstance) original);
	}

	// todo: needs verification via debug that this works as intended.
	@ModifyExpressionValue(method = "createOverride", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$createOverride(Object original, @Local(ordinal = 0, argsOnly = true) EntityAttribute attribute) {
		Identifier identifier = Registries.ATTRIBUTE.getId(attribute);
		return this.data_instances.getOrDefault(identifier, (EntityAttributeInstance) original);
	}

	@ModifyReturnValue(method = "has", at = @At("RETURN"))
	private boolean data_attributes$has(boolean original, EntityAttribute type) {
		Identifier identifier = Registries.ATTRIBUTE.getId(type);
		return original || this.data_instances.containsKey(identifier);
	}

	@ModifyReturnValue(method = "hasModifier", at = @At("RETURN"))
	private boolean data_attributes$hasModifier(boolean original, EntityAttribute type, UUID uuid) {
		Identifier identifier = Registries.ATTRIBUTE.getId(type);
		var instance = this.data_instances.get(identifier);
		return original || (instance != null && instance.getModifier(uuid) != null);
	}

	@Override
	public void data_attributes$copy(DefaultAttributeContainer.Builder builder) {
		for (EntityAttribute entityAttribute : this.instances.keySet()) {
			EntityAttributeInstance entityAttributeInstance = this.instances.get(entityAttribute);
			double value = entityAttributeInstance.getBaseValue();
			builder.add(entityAttribute, value);
		}
	}
}
