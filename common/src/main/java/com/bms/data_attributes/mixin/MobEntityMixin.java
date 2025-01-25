package com.bms.data_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.bms.data_attributes.api.item.ItemHelper;

@Mixin(Mob.class)
abstract class MobEntityMixin {
    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 0))
    private double data_attributes$getAttackDamage_sword_new(double original, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Integer value = ((ItemHelper) newStack.getItem()).getAttackDamage(newStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 1))
    private double data_attributes$getAttackDamage_sword_old(double original, @Local(ordinal = 0, argsOnly = true) ItemStack oldStack) {
    	Integer value = ((ItemHelper) oldStack.getItem()).getAttackDamage(oldStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 2))
    private double data_attributes$getAttackDamage_digger_new(double original, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Integer value = ((ItemHelper) newStack.getItem()).getAttackDamage(newStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 3))
    private double data_attributes$getAttackDamage_digger_old(double original, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
    	Integer value = ((ItemHelper) oldStack.getItem()).getAttackDamage(oldStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getDefense()I", ordinal = 0))
    private int data_attributes$getProtection_0(int original, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Integer value = ((ItemHelper) newStack.getItem()).getDefense(newStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getDefense()I", ordinal = 1))
    private int data_attributes$getProtection_1(int original, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
        Integer value = ((ItemHelper) oldStack.getItem()).getDefense(oldStack);
        return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getDefense()I", ordinal = 2))
    private int data_attributes$getProtection_2(int og, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Integer value = ((ItemHelper) newStack.getItem()).getDefense(newStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getDefense()I", ordinal = 3))
    private int data_attributes$getProtection_3(int og, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
    	Integer value = ((ItemHelper) oldStack.getItem()).getDefense(oldStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getToughness()F", ordinal = 0))
    private float data_attributes$getToughness_0(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Float value = ((ItemHelper) armorItem).getToughness(newStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getToughness()F", ordinal = 1))
    private float data_attributes$getToughness_1(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
    	Float value = ((ItemHelper) armorItem2).getToughness(oldStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getToughness()F",ordinal = 2))
    private float data_attributes$getToughness_2(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Float value = ((ItemHelper) armorItem).getToughness(newStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorItem;getToughness()F", ordinal = 3))
    private float data_attributes$getToughness_3(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
    	Float value = ((ItemHelper) armorItem2).getToughness(oldStack);
    	return value != null ? value : og;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 2))
    private double data_attributes$getAttackDamage_0(double original, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
    	Integer value = ((ItemHelper) newStack.getItem()).getAttackDamage(newStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getApproximateAttackDamageWithItem(Lnet/minecraft/world/item/ItemStack;)D", ordinal = 3))
    private double data_attributes$getAttackDamage_1(double original, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
    	Integer value = ((ItemHelper) oldStack.getItem()).getAttackDamage(oldStack);
    	return value != null ? value : original;
    }

    @ModifyExpressionValue(method = "canReplaceEqualItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getDamageValue()I"))
    private int data_attributes$getAttackDamage_2(int original, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
    	Integer value = ((ItemHelper) stack.getItem()).getAttackDamage(stack);
    	return value != null ? value : original;
    }
}
