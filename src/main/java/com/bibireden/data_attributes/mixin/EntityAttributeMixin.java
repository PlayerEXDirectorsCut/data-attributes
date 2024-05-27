package com.bibireden.data_attributes.mixin;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bibireden.data_attributes.api.attribute.DynamicSkillAttribute;
import com.bibireden.data_attributes.api.attribute.DynamicEntityAttribute;
import com.bibireden.data_attributes.api.enums.FunctionBehavior;
import com.bibireden.data_attributes.api.enums.StackingFormula;
import com.bibireden.data_attributes.api.events.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;
import com.bibireden.data_attributes.registry.AttributeSkillData;
import com.bibireden.data_attributes.registry.AttributeOverrideData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.math.MathHelper;

@Mixin(EntityAttribute.class)
abstract class EntityAttributeMixin implements MutableEntityAttribute {

    // Unique fields to store additional attribute data
    @Unique private Map<DynamicEntityAttribute, AttributeSkillData> data_parents, data_children;
    @Unique private StackingFormula data_stackingFormula;
    @Unique private FunctionBehavior data_functionBehavior;
    @Unique private String data_translationKey;
    @Unique protected double data_fallbackValue, data_minValue, data_maxValue, data_incrementValue;

    // Original fields from the EntityAttribute class
    @Final
    @Shadow
    private double fallback;

    @Final
    @Shadow
    private String translationKey;

    // Constructor injection to initialize additional fields
    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_init(String translationKey, double fallback, CallbackInfo ci) {
        this.data_translationKey = translationKey;
        this.data_fallbackValue = fallback;
        this.data_minValue = Double.MIN_VALUE;
        this.data_maxValue = Double.MAX_VALUE;
        this.data_stackingFormula = StackingFormula.Flat;
        this.data_functionBehavior = FunctionBehavior.Add;
        this.data_parents = new Object2ObjectArrayMap<>();
        this.data_children = new Object2ObjectArrayMap<>();
    }

    // Injection to override the default value
    @Inject(method = "getDefaultValue", at = @At("HEAD"), cancellable = true)
    private void data_getDefaultValue(CallbackInfoReturnable<Double> ci) {
        ci.setReturnValue(this.data_fallbackValue);
    }

    // Injection to always consider the attribute as tracked
    @Inject(method = "isTracked", at = @At("HEAD"), cancellable = true)
    private void data_isTracked(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(true);
    }

    // Injection to clamp attribute values and trigger an event
    @Inject(method = "clamp", at = @At("HEAD"), cancellable = true)
    private void data_clamp(double value, CallbackInfoReturnable<Double> ci) {
        ci.setReturnValue(this.data_clamped(value));
    }

    // Injection to get the custom translation key
    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    private void data_getTranslationKey(CallbackInfoReturnable<String> ci) {
        ci.setReturnValue(this.data_translationKey);
    }

    // Custom method to clamp attribute values and trigger an event
    protected double data_clamped(double valueIn) {
        double value = EntityAttributeModifiedEvents.Companion.getCLAMPED().invoker().onClamped((EntityAttribute)(Object)this, valueIn);
        return MathHelper.clamp(value, this.minValue(), this.maxValue());
    }

    // Override method to modify attribute properties
    @Override
    public void override(AttributeOverrideData overrideData) {
        this.data_translationKey = overrideData.getTranslationKey();
        this.data_minValue = overrideData.getMin();
        this.data_maxValue = overrideData.getMax();
        this.data_incrementValue = overrideData.getIncrement();
        this.data_fallbackValue = overrideData.getDefault();
        this.data_functionBehavior = overrideData.getBehavior();
        this.data_stackingFormula = overrideData.getFormula();
    }

//    // Method to set additional properties for the attribute
//    @Override
//    public void properties(Map<String, String> properties) {
//        if(properties == null) return;
//        this.data_properties = properties;
//    }

    // Method to add a parent attribute with an associated function
    @Override
    public void addParent(MutableEntityAttribute attributeIn, AttributeSkillData function) {
        this.data_parents.put(attributeIn, function);
    }

    // Method to add a child attribute with an associated function
    @Override
    public void addChild(MutableEntityAttribute attributeIn, AttributeSkillData function) {
        if(this.contains(this, attributeIn)) return;

        attributeIn.addParent(this, function);
        this.data_children.put(attributeIn, function);
    }

    // Method to clear and reset attribute properties
    @Override
    public void clear() {
        this.override(new AttributeOverrideData(this.fallback, this.fallback, this.fallback, 0.0D, FunctionBehavior.Add, StackingFormula.Flat, this.translationKey));
        this.data_parents.clear();
        this.data_children.clear();
    }

    // Method to calculate the sum of two attribute values based on stacking behavior

//    @Override
//    public void sum(double k, double k2, double v, double v2) {
//        this.data_stackingFormula.result(k, k2, v, v2, this.data_incrementValue);
//    }

    // Method to check if one attribute contains another (checks parent-child relationships)
    @Override
    public boolean contains(MutableEntityAttribute a, MutableEntityAttribute b) {
        if(b == null || a == b) return true;

        for(DynamicEntityAttribute n : a.parentsMutable().keySet()) {
            MutableEntityAttribute m = (MutableEntityAttribute)n;

            if(m.contains(m, b)) return true;
        }

        return false;
    }

    // Method to get the mutable map of parent attributes
    @Override
    public Map<DynamicEntityAttribute, AttributeSkillData> parentsMutable() {
        return this.data_parents;
    }

    // Method to get the mutable map of child attributes
    @Override
    public Map<DynamicEntityAttribute, AttributeSkillData> childrenMutable() {
        return this.data_children;
    }

    // Method to get the minimum value of the attribute
    @Override
    public double minValue() {
        return this.data_minValue;
    }

    // Method to get the maximum value of the attribute
    @Override
    public double maxValue() {
        return this.data_maxValue;
    }

    @NotNull
    @Override
    public FunctionBehavior behavior() {
        return this.data_functionBehavior;
    }

    // Method to get the stacking behavior of the attribute
    @Override
    public StackingFormula formula() {
        return this.data_stackingFormula;
    }

    // Method to get an immutable map of parent attributes and their functions

    @NotNull
    @Override
    public Map<DynamicEntityAttribute, DynamicSkillAttribute> parents() {
        return ImmutableMap.copyOf(this.data_parents);
    }

    // Method to get an immutable map of child attributes and their functions
    @Override
    public Map<DynamicEntityAttribute, DynamicSkillAttribute> children() {
        return ImmutableMap.copyOf(this.data_children);
    }
}