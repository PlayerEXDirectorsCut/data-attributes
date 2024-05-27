package com.bibireden.data_attributes.mixin;

import java.util.UUID;
import java.util.function.Supplier;

import com.bibireden.data_attributes.mutable.MutableAttributeModifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.attribute.EntityAttributeModifier;

@Mixin(EntityAttributeModifier.class)
abstract class EntityAttributeModifierMixin implements MutableAttributeModifier {

    // Custom field to store the modified value
    @Unique
    private double data_value;

    // Injection to capture the value during object creation
    @Inject(method = "<init>(Ljava/util/UUID;Ljava/util/function/Supplier;DLnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;)V", at = @At("TAIL"))
    private void init(UUID uuid, Supplier<String> nameGetter, double value, EntityAttributeModifier.Operation operation, CallbackInfo ci) {
        this.data_value = value;
    }

    // Injection to override the original getValue method
    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void onGetValue(CallbackInfoReturnable<Double> ci) {
        ci.setReturnValue(this.data_value);
    }

    // Redirects to override the original toString and toNbt methods
    @Redirect(method = "toString", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;value:D", opcode = Opcodes.GETFIELD))
    private double onToString(EntityAttributeModifier modifier) {
        return this.data_value;
    }

    @Redirect(method = "toNbt", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;value:D", opcode = Opcodes.GETFIELD))
    private double onToNbt(EntityAttributeModifier modifier) {
        return this.data_value;
    }

    // Custom method to update the stored value
    @Override
    public void update(double value) {
        this.data_value = value;
    }
}