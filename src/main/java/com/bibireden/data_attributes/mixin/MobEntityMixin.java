package com.bibireden.data_attributes.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.bibireden.data_attributes.api.item.ItemHelper;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;

@Mixin(MobEntity.class)
abstract class MobEntityMixin {
	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 0))
	private float data_getAttackDamage_0(float original, @Local(ordinal = 0) SwordItem swordItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) swordItem).getAttackDamage(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 1))
	private float data_getAttackDamage_1(float original, @Local(ordinal = 1) SwordItem swordItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) swordItem2).getAttackDamage(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 2))
	private float data_getAttackDamage_2(float original, @Local(ordinal = 0) SwordItem swordItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) swordItem).getAttackDamage(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 3))
	private float data_getAttackDamage_3(float original, @Local(ordinal = 1) SwordItem swordItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) swordItem2).getAttackDamage(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 0))
	private int data_getProtection_0(int og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) armorItem).getProtection(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 1))
	private int data_getProtection_1(int og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) armorItem2).getProtection(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 2))
	private int data_getProtection_2(int og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) armorItem).getProtection(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 3))
	private int data_getProtection_3(int og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) armorItem2).getProtection(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 0))
	private float data_getToughness_0(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) armorItem).getToughness(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 1))
	private float data_getToughness_1(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) armorItem2).getToughness(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 2))
	private float data_getToughness_2(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) armorItem).getToughness(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 3))
	private float data_getToughness_3(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) armorItem2).getToughness(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 0))
	private float data_getAttackDamage_0(float og, @Local(ordinal = 0) MiningToolItem miningToolItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) miningToolItem).getAttackDamage(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 1))
	private float data_getAttackDamage_1(float og, @Local(ordinal = 1) MiningToolItem miningToolItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) miningToolItem2).getAttackDamage(oldStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 2))
	private float data_getAttackDamage_2(float og, @Local(ordinal = 0) MiningToolItem miningToolItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		return ((ItemHelper) miningToolItem).getAttackDamage(newStack);
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 3))
	private float data_getAttackDamage_3(float og, @Local(ordinal = 1) MiningToolItem miningToolItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		return ((ItemHelper) miningToolItem2).getAttackDamage(oldStack);
	}
}
