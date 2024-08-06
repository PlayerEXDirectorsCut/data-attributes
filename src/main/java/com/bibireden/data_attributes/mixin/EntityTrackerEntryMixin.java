package com.bibireden.data_attributes.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	@Final
	@Shadow
	private Entity entity;

	@Inject(method = "syncEntityData", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
	private void data_attributes$syncEntityData(CallbackInfo ci) { ((LivingEntity) this.entity).getAttributes().data_attributes$clearTracked(); }
}
