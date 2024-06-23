package com.bibireden.data_attributes.mixin;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.bibireden.data_attributes.mutable.MutableAttributeContainer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	@Final
	@Shadow
	private Entity entity;

	@WrapOperation(method = "syncEntityData", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
	private void data_syncEntityData(Set<EntityAttributeInstance> instances, Operation<Void> original) {
		MutableAttributeContainer container = (MutableAttributeContainer) ((LivingEntity) this.entity).getAttributes();
		container.data_attributes$clearTracked();
		original.call(instances);
	}
}
