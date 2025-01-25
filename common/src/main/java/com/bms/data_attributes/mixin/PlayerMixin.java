package com.bms.data_attributes.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerMixin {
	@Shadow
	@Final
	private Abilities abilities;

	@Inject(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttribute(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;"))
	private void data_attributes$readCustomDataFromNbt(CompoundTag compound, CallbackInfo ci) {
		this.abilities.setWalkingSpeed((float) ((Player) (Object) this).getAttributeBaseValue(Attributes.MOVEMENT_SPEED));
	}
}
