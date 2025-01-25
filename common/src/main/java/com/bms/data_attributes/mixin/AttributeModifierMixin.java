package com.bms.data_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.mutable.MutableAttributeModifier;

@Mixin(AttributeModifier.class)
abstract class AttributeModifierMixin implements MutableAttributeModifier {
    @Shadow @Final private ResourceLocation id;
    @Shadow @Final private AttributeModifier.Operation operation;
    @Unique
    private double data_attributes$value;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ResourceLocation id, double value, AttributeModifier.Operation operation, CallbackInfo ci) {
        this.data_attributes$value = value;
    }

    @ModifyReturnValue(method = "amount", at = @At("RETURN"))
    private double data_attributes$amount(double original) { return this.data_attributes$value; }

    @Override
    public void data_attributes$updateValue(double value) {
        this.data_attributes$value = value;
    }

    @WrapMethod(method = "save")
    private CompoundTag data_attributes$save(Operation<CompoundTag> original) {
        return (CompoundTag) AttributeModifier.CODEC.encode(new AttributeModifier(this.id, this.data_attributes$value, this.operation), NbtOps.INSTANCE, new CompoundTag()).getOrThrow();
    }
}
