package com.bms.data_attributes.mixin.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.api.item.ItemHelper;


@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    private void data_init(ItemLike item, int count, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        if (item != null) {
            ((ItemHelper) item.asItem()).onStackCreated(stack, count);
        }
    }
}