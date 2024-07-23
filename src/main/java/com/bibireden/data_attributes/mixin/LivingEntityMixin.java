package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.DataAttributesClient;
import com.bibireden.data_attributes.config.AttributeConfigManager;
import com.bibireden.data_attributes.ext.LivingEntityKt;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.DataAttributes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 999)
abstract class LivingEntityMixin {
	@Shadow public abstract AttributeContainer getAttributes();

	@Final @Shadow @Mutable private AttributeContainer attributes;

	@Unique
	private int data_attributes$update_flag;

	@SuppressWarnings("UnreachableCode")
	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity)(Object)this;
		this.attributes = DataAttributes.getManagerFromWorld(entity.getWorld()).getContainer(entityType, entity);
		this.data_attributes$update_flag = DataAttributes.getManagerFromWorld(entity.getWorld()).getUpdateFlag();
	}

	@SuppressWarnings("UnreachableCode")
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickActiveItemStack()V"))
	private void data_attributes$tick(CallbackInfo ci) {
		LivingEntity entity = ((LivingEntity) (Object) this);
		AttributeConfigManager manager = DataAttributes.getManagerFromWorld(entity.getWorld());
		final int updateFlag = manager.getUpdateFlag();

		if (this.data_attributes$update_flag != updateFlag) {
			this.data_attributes$update_flag = updateFlag;

			@SuppressWarnings("unchecked")
			AttributeContainer handledContainer = DataAttributes.getManagerFromWorld(entity.getWorld()).getContainer((EntityType<? extends LivingEntity>) entity.getType(), entity);

			handledContainer.setFrom(this.getAttributes());
			this.attributes = handledContainer;

			LivingEntityKt.refreshAttributes(entity);
		}
	}
}
