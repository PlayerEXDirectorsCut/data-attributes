package com.bibireden.data_attributes.mixin;

import com.bibireden.data_attributes.DataAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 999)
abstract class LivingEntityMixin {

    // Marking 'attributes' as @Mutable to allow modification
    @Mutable
    @Shadow
    private AttributeContainer attributes;

    // Unique field to store the update flag
    @Unique
    private int data_updateFlag;

    // Injection at the end of the constructor
    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_init(EntityType<? extends LivingEntity> entityType, World world, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object) this;
        // Initializing the attributes container using DataAttributes
        this.attributes = DataAttributes.Companion.getRegistry().getContainer(entityType, livingEntity);
        // Initializing the update flag
        this.data_updateFlag = DataAttributes.Companion.getRegistry().updateFlag();
        // Setting the entity's health to its maximum health
        livingEntity.setHealth(livingEntity.getMaxHealth());
    }

    // Injection at the start of the 'tick' method
    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tickActiveItemStack()V"))
    private void data_tick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity)(Object)this;
        // Getting the current update flag from DataAttributes
        final int updateFlag = DataAttributes.Companion.getRegistry().updateFlag();

        // Checking if the update flag has changed since the last tick
        if (this.data_updateFlag != updateFlag) {
            // Getting the attribute container from the entity
            AttributeContainer container = livingEntity.getAttributes();

            // Getting the modified attribute container from DataAttributes
            @SuppressWarnings("unchecked")
            AttributeContainer container2 = DataAttributes.Companion.getRegistry().getContainer((EntityType<? extends LivingEntity>) livingEntity.getType(), livingEntity);

            // Setting the attributes of the entity to the modified container
            container2.setFrom(container);
            this.attributes = container2;

            // Updating the update flag
            this.data_updateFlag = updateFlag;

            // Refreshing the entity's attributes using DataAttributes
            DataAttributes.Companion.refreshAttributes(livingEntity);
        }
    }
}