package com.bibireden.data_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.mutable.MutableAttributeModifier;

import net.minecraft.entity.attribute.EntityAttributeModifier;

// todo: Due to Codec implementation, some methods were removed.
//  Investigate if modifiers get applied properly with the new system.
@Mixin(EntityAttributeModifier.class)
abstract class EntityAttributeModifierMixin implements MutableAttributeModifier {
    @Unique
    private double data_attributes$value;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(Identifier identifier, double value, EntityAttributeModifier.Operation operation, CallbackInfo ci) {
        this.data_attributes$value = value;
    }

    @ModifyReturnValue(method = "value", at = @At("RETURN"))
    private double data_attributes$getValue(double original) { return this.data_attributes$value; }

    @Override
    public void data_attributes$updateValue(double value) {
        this.data_attributes$value = value;
    }
}
