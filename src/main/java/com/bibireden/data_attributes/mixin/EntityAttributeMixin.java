package com.bibireden.data_attributes.mixin;

import java.util.Map;

import com.bibireden.data_attributes.data.AttributeFunction;
import com.bibireden.data_attributes.data.AttributeOverride;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.bibireden.data_attributes.api.attribute.IAttributeFunction;
import com.bibireden.data_attributes.api.attribute.IEntityAttribute;
import com.bibireden.data_attributes.api.attribute.StackingFormula;
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;
import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.math.MathHelper;

@Mixin(EntityAttribute.class)
abstract class EntityAttributeMixin implements MutableEntityAttribute {
    @Unique private Map<IEntityAttribute, AttributeFunction> data_parents, data_children;
    @Unique private StackingFormula data_formula;
    @Unique private String data_translationKey;
    @Unique protected double data_midpoint, data_min, data_max, data_smoothness;

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
        this.data_midpoint = fallback;
        this.data_min = Double.MIN_VALUE;
        this.data_max = Double.MAX_VALUE;
        this.data_formula = StackingFormula.Flat;
        this.data_parents = new Object2ObjectArrayMap<>();
        this.data_children = new Object2ObjectArrayMap<>();
    }
    
    // Injection to override the default value
    @Inject(method = "getDefaultValue", at = @At("HEAD"), cancellable = true)
    private void data_attributes$getDefaultValue(CallbackInfoReturnable<Double> ci) {
        ci.setReturnValue(this.data_midpoint);
    }
    
    // Injection to always consider the attribute as tracked
    @Inject(method = "isTracked", at = @At("HEAD"), cancellable = true)
    private void data_attributes$isTracked(CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(true);
    }
    
    // Injection to clamp attribute values and trigger an event
    @Inject(method = "clamp", at = @At("HEAD"), cancellable = true)
    private void data_attributes$clamp(double value, CallbackInfoReturnable<Double> ci) {
        ci.setReturnValue(this.data_attributes$clamped(value));
    }
    
    // Injection to get the custom translation key
    @Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
    private void data_attributes$getTranslationKey(CallbackInfoReturnable<String> ci) {
        ci.setReturnValue(this.data_translationKey);
    }
    
    // Custom method to clamp attribute values and trigger an event
    @Unique
    protected double data_attributes$clamped(double valueIn) {
        double value = EntityAttributeModifiedEvents.CLAMPED.invoker().onClamped((EntityAttribute)(Object)this, valueIn);
        return MathHelper.clamp(value, this.data_attributes$min(), this.data_attributes$max());
    }
    
    // Override method to modify attribute properties
    @Override
    public void data_attributes$override(AttributeOverride override) {
        this.data_translationKey = override.getTranslationKey();
        this.data_min = override.getMin();
        this.data_max = override.getMax();
        this.data_smoothness = override.getSmoothness();
        this.data_midpoint = override.getMidpoint();
        this.data_formula = override.getFormula();
    }
    
    // Method to add a parent attribute with an associated function
    @Override
    public void data_attributes$addParent(MutableEntityAttribute attributeIn, AttributeFunction function) {
        this.data_parents.put(attributeIn, function);
    }
    
    // Method to add a child attribute with an associated function
    @Override
    public void data_attributes$addChild(MutableEntityAttribute attributeIn, AttributeFunction function) {
        if(MutableEntityAttribute.contains(this, attributeIn)) return;
        attributeIn.data_attributes$addParent(this, function);
        this.data_children.put(attributeIn, function);
    }

    // Clears parent-child relationships of this clamped attribute
    @Unique
    public void data_attributes$clearDescendants() {
        this.data_parents.clear();
        this.data_children.clear();
    }

    // Method to clear and reset attribute properties
    @Override
    public void data_attributes$clear() {
        this.data_attributes$override(new AttributeOverride(this.fallback, this.fallback, this.fallback, 0.0D, StackingFormula.Flat, this.translationKey));
        this.data_attributes$clearDescendants();
    }
    
    // Method to calculate the sum of two attribute values based on stacking behavior
    @Override
    public double data_attributes$sum(final double k, final double k2, final double v, final double v2) {
        return this.data_formula.result(k, k2, v, v2, this.data_smoothness);
    }
    
    // Method to get the mutable map of parent attributes
    @Override
    public Map<IEntityAttribute, AttributeFunction> data_attributes$parentsMutable() {
        return this.data_parents;
    }
    
    // Method to get the mutable map of child attributes
    @Override
    public Map<IEntityAttribute, AttributeFunction> data_attributes$childrenMutable() {
        return this.data_children;
    }
    
    // Method to get the minimum value of the attribute
    @Override
    public double data_attributes$min() {
        return this.data_min;
    }
    
    // Method to get the maximum value of the attribute
    @Override
    public double data_attributes$max() {
        return this.data_max;
    }
    
    // Method to get the stacking behavior of the attribute
    @Override
    public StackingFormula data_attributes$formula() {
        return this.data_formula;
    }
    
    // Method to get an immutable map of parent attributes and their functions
    @Override
    public Map<IEntityAttribute, IAttributeFunction> data_attributes$parents() {
        return ImmutableMap.copyOf(this.data_parents);
    }
    
    // Method to get an immutable map of child attributes and their functions
    @Override
    public Map<IEntityAttribute, IAttributeFunction> data_attributes$children() {
        return ImmutableMap.copyOf(this.data_children);
    }
}
