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
	private float data_attributes$getAttackDamage_0(float og, @Local(ordinal = 0) SwordItem swordItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) swordItem).getAttackDamage(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 1))
	private float data_attributes$getAttackDamage_1(float og, @Local(ordinal = 1) SwordItem swordItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) swordItem2).getAttackDamage(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 2))
	private float data_attributes$getAttackDamage_2(float og, @Local(ordinal = 0) SwordItem swordItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) swordItem).getAttackDamage(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/SwordItem;getAttackDamage()F", ordinal = 3))
	private float data_attributes$getAttackDamage_3(float og, @Local(ordinal = 1) SwordItem swordItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) swordItem2).getAttackDamage(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 0))
	private int data_attributes$getProtection_0(int og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Integer value = ((ItemHelper) armorItem).getProtection(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 1))
	private int data_attributes$getProtection_1(int og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Integer value = ((ItemHelper) armorItem2).getProtection(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 2))
	private int data_attributes$getProtection_2(int og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Integer value = ((ItemHelper) armorItem).getProtection(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getProtection()I", ordinal = 3))
	private int data_attributes$getProtection_3(int og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Integer value = ((ItemHelper) armorItem2).getProtection(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 0))
	private float data_attributes$getToughness_0(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) armorItem).getToughness(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 1))
	private float data_attributes$getToughness_1(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) armorItem2).getToughness(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 2))
	private float data_attributes$getToughness_2(float og, @Local(ordinal = 0) ArmorItem armorItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) armorItem).getToughness(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorItem;getToughness()F", ordinal = 3))
	private float data_attributes$getToughness_3(float og, @Local(ordinal = 1) ArmorItem armorItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) armorItem2).getToughness(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 0))
	private float data_attributes$getAttackDamage_0(float og, @Local(ordinal = 0) MiningToolItem miningToolItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) miningToolItem).getAttackDamage(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 1))
	private float data_attributes$getAttackDamage_1(float og, @Local(ordinal = 1) MiningToolItem miningToolItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) miningToolItem2).getAttackDamage(oldStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 2))
	private float data_attributes$getAttackDamage_2(float og, @Local(ordinal = 0) MiningToolItem miningToolItem, @Local(ordinal = 0, argsOnly = true) ItemStack newStack) {
		Float value = ((ItemHelper) miningToolItem).getAttackDamage(newStack);
		return value != null ? value : og;
	}

	@ModifyExpressionValue(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/MiningToolItem;getAttackDamage()F", ordinal = 3))
	private float data_attributes$getAttackDamage_3(float og, @Local(ordinal = 1) MiningToolItem miningToolItem2, @Local(ordinal = 1, argsOnly = true) ItemStack oldStack) {
		Float value = ((ItemHelper) miningToolItem2).getAttackDamage(oldStack);
		return value != null ? value : og;
	}
}
