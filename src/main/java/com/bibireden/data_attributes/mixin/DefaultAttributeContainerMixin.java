package com.bibireden.data_attributes.mixin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.mutable.MutableDefaultAttributeContainer;

import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

/** Swaps out the default instances map for a custom one that has an {@link Identifier} as a key instead. */
@Mixin(DefaultAttributeContainer.class)
abstract class DefaultAttributeContainerMixin implements MutableDefaultAttributeContainer {
	@Unique
	private Map<Identifier, EntityAttributeInstance> data_attributes$instances;

	@Final
	@Shadow
	private Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(Map<RegistryEntry<EntityAttribute>, EntityAttributeInstance> instances, CallbackInfo ci) {
		this.data_attributes$instances = new HashMap<>();
		instances.forEach((entry, instance) -> {
			Identifier key = Registries.ATTRIBUTE.getId(entry.value());
			if (key != null) {
				this.data_attributes$instances.put(key, instance);
			}
		});
	}

	@ModifyExpressionValue(method = "require", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$require(Object original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> entry) {
		Identifier identifier = Registries.ATTRIBUTE.getId(entry.value());
		return this.data_attributes$instances.getOrDefault(identifier, (EntityAttributeInstance) original);
	}

	@ModifyExpressionValue(method = "createOverride", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
	private Object data_attributes$createOverride(Object original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> entry) {
		Identifier identifier = Registries.ATTRIBUTE.getId(entry.value());
		return this.data_attributes$instances.getOrDefault(identifier, (EntityAttributeInstance) original);
	}

	@ModifyReturnValue(method = "has", at = @At("RETURN"))
	private boolean data_attributes$has(boolean original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> entry) {
		Identifier identifier = Registries.ATTRIBUTE.getId(entry.value());
		return this.data_attributes$instances.containsKey(identifier) || original;
	}

	@ModifyReturnValue(method = "hasModifier", at = @At("RETURN"))
	private boolean data_attributes$hasModifier(boolean original, @Local(argsOnly = true) RegistryEntry<EntityAttribute> entry, @Local(argsOnly = true) Identifier id) {
		Identifier identifier = Registries.ATTRIBUTE.getId(entry.value());
		var instance = this.data_attributes$instances.get(identifier);
		return (instance != null && instance.getModifier(id) != null) || original;
	}

	@Override
	public void data_attributes$copy(DefaultAttributeContainer.Builder builder) {
		this.instances.forEach((entry, instance) -> builder.add(entry, instance.getBaseValue()));
	}
}
