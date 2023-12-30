package com.github.clevernucleus.dataattributes.mixin.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.dataattributes.api.item.ItemHelper;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    // Injects code at the end of the ItemStack constructor
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    private void data_init(ItemConvertible item, int count, CallbackInfo ci) {
        // Casts 'this' to ItemStack
        ItemStack stack = (ItemStack) (Object) this;

        if (item != null) {
            // Calls onStackCreated from ItemHelper when an ItemStack is created
            ((ItemHelper) item.asItem()).onStackCreated(stack, count);
        }
    }
}
