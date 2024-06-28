package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.ext.LivingEntityKt;
import net.minecraft.entity.data.TrackedData;
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
	@Final
	@Mutable
	@Shadow
	private AttributeContainer attributes;

	@Shadow @Final private static TrackedData<Float> HEALTH;

	@Shadow public abstract float getHealth();

	@Shadow public abstract float getMaxHealth();

	@Unique
	private int data_updateFlag;

	@SuppressWarnings("ALL") // todo: until intellij updates
	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
        // Initializing the attributes container using DataAttributes
		this.attributes = DataAttributes.SERVER_MANAGER.getContainer(entityType, livingEntity);
        // Initializing the update flag
		this.data_updateFlag = DataAttributes.SERVER_MANAGER.getUpdateFlag();
        // Setting the entity's health to its maximum health
		livingEntity.setHealth(livingEntity.getMaxHealth());
	}
	
	@SuppressWarnings("ALL") // todo: until intellij updates
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickActiveItemStack()V"))
	private void data_tick(CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;

		final int updateFlag = DataAttributes.SERVER_MANAGER.getUpdateFlag();
		
		if (this.data_updateFlag != updateFlag) {
			AttributeContainer container = livingEntity.getAttributes();
			
			@SuppressWarnings("unchecked")
			AttributeContainer container2 = DataAttributes.SERVER_MANAGER.getContainer((EntityType<? extends LivingEntity>) livingEntity.getType(), livingEntity);
            
			container2.setFrom(container);
			this.attributes = container2;
            
			this.data_updateFlag = updateFlag;

			LivingEntityKt.refreshAttributes(livingEntity);
		}
	}
}
