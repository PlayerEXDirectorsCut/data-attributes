package com.bibireden.data_attributes.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.bibireden.data_attributes.mutable.MutableAttributeContainer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.network.EntityTrackerEntry;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	
    // Shadowed field to access the 'entity' field in EntityTrackerEntry
	@Final
	@Shadow
	private Entity entity;

    // Redirects the 'clear' method call on the Set<EntityAttributeInstance>
	@Redirect(method = "syncEntityData", at = @At(value = "INVOKE", target = "Ljava/util/Set;clear()V"))
	private void data_syncEntityData(Set<EntityAttributeInstance> set) {
        // Accessing the attribute container of the living entity
		MutableAttributeContainer container = (MutableAttributeContainer) ((LivingEntity) this.entity).getAttributes();
		
        // Custom method to clear tracked attributes
		container.data_attributes$clearTracked();
	}
}
