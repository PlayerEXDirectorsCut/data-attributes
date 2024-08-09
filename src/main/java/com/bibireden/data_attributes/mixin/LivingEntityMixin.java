package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.data_attributes.config.AttributeConfigManager;
import com.bibireden.data_attributes.ext.LivingEntityKt;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 999)
abstract class LivingEntityMixin {
	@Unique
	private int data_attributes$update_flag;

	@Final @Shadow @Mutable private AttributeContainer attributes;

	@Shadow public abstract AttributeContainer getAttributes();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		AttributeConfigManager manager = DataAttributesAPI.getManager(entity.getWorld());
		this.attributes = manager.getContainer(entityType, entity);
		this.data_attributes$update_flag = manager.getUpdateFlag();
	}

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickActiveItemStack()V"))
	private void data_attributes$tick(CallbackInfo ci) {
		LivingEntity entity = (LivingEntity) (Object) this;
		AttributeConfigManager manager = DataAttributesAPI.getManager(entity.getWorld());
		final int updateFlag = manager.getUpdateFlag();

		if (this.data_attributes$update_flag != updateFlag) {
			this.data_attributes$update_flag = updateFlag;

			@SuppressWarnings("unchecked")
			AttributeContainer container = manager.getContainer((EntityType<? extends LivingEntity>) entity.getType(), entity);
			container.setFrom(this.getAttributes());

			this.attributes = container;

			LivingEntityKt.refreshAttributes(entity);
		}
	}
}
