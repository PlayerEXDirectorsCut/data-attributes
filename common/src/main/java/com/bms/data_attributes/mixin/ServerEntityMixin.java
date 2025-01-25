package com.bms.data_attributes.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
abstract class ServerEntityMixin {
	@Final
	@Shadow
	private Entity entity;

	@Inject(method = "sendDirtyEntityData", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
	private void data_attributes$syncEntityData(CallbackInfo ci) { ((LivingEntity) this.entity).getAttributes().data_attributes$clearTracked(); }
}
