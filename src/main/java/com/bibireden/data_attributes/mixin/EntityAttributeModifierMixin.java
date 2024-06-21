package com.bibireden.data_attributes.mixin;

import java.util.UUID;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.mutable.MutableAttributeModifier;

import net.minecraft.entity.attribute.EntityAttributeModifier;

@Mixin(EntityAttributeModifier.class)
abstract class EntityAttributeModifierMixin implements MutableAttributeModifier {
    @Unique
    private double data_value;

    @Inject(method = "<init>(Ljava/util/UUID;Ljava/util/function/Supplier;DLnet/minecraft/entity/attribute/EntityAttributeModifier$Operation;)V", at = @At("TAIL"))
    private void init(UUID uuid, Supplier<String> nameGetter, double value, EntityAttributeModifier.Operation operation, CallbackInfo ci) {
        this.data_value = value;
    }

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double data_attributes$getValue(double original) { return this.data_value; }

    @ModifyExpressionValue(method = "toString", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;value:D"))
    private double data_attributes$toString(double original) { return this.data_value; }

    @ModifyExpressionValue(method = "toNbt", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributeModifier;value:D"))
    private double data_attributes$toNbt(double original) { return this.data_value; }

    @Override
    public void data_attributes$updateValue(double value) {
        this.data_value = value;
    }
}
