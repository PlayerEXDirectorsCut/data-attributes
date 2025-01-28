package com.bms.data_attributes.mixin;

import java.util.Map;

import com.bms.data_attributes.api.attribute.AttributeFormat;
import com.bms.data_attributes.config.models.OverridesConfigModel.AttributeOverride;
import com.bms.data_attributes.config.functions.AttributeFunction;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.api.attribute.IAttribute;
import com.bms.data_attributes.api.attribute.StackingFormula;
import com.bms.data_attributes.api.event.AttributeModifiedEvents;
import com.bms.data_attributes.mutable.MutableAttribute;
import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

@Mixin(Attribute.class)
abstract class AttributeMixin implements MutableAttribute {
    @Unique private Map<IAttribute, AttributeFunction> data_attributes$parents, data_attributes$children;
    @Unique protected StackingFormula data_attributes$formula;
    @Unique protected AttributeFormat data_attributes$format;
    @Unique protected boolean data_attributes$enabled;
    @Unique protected double data_attributes$min, data_attributes$max, data_attributes$smoothness;

    @Final
    @Shadow
    private double defaultValue;
    
    // Constructor injection to initialize additional fields
    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_attributes$init(String translationKey, double fallback, CallbackInfo ci) {
        this.data_attributes$min = Double.MIN_VALUE;
        this.data_attributes$max = Double.MAX_VALUE;
        this.data_attributes$formula = StackingFormula.Flat;
        this.data_attributes$format = AttributeFormat.Whole;
        this.data_attributes$parents = new Object2ObjectArrayMap<>();
        this.data_attributes$children = new Object2ObjectArrayMap<>();
    }

    @ModifyReturnValue(method = "isClientSyncable", at = @At("RETURN"))
    private boolean data_attributes$isTracked(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "sanitizeValue", at = @At("RETURN"))
    private double data_attributes$clamp(double original) {
        return this.data_attributes$enabled ? this.data_attributes$clamped(original) : original;
    }

    @Unique
    protected double data_attributes$clamped(double valueIn) {
        // todo: unify with bus & modified event
        double value = AttributeModifiedEvents.Clamped.stream.sink().onClamped((Attribute) (Object) this, valueIn);
        return Mth.clamp(value, this.data_attributes$min(), this.data_attributes$max());
    }

    @Override
    public void data_attributes$override(AttributeOverride override) {
        this.data_attributes$enabled = override.enabled;
        this.data_attributes$min = override.min;
        this.data_attributes$max = override.max;
        this.data_attributes$smoothness = override.smoothness;
        this.data_attributes$formula = override.formula;
        this.data_attributes$format = override.format;
    }

    @Override
    public void data_attributes$addParent(MutableAttribute attribute, AttributeFunction function) {
        this.data_attributes$parents.put(attribute, function);
    }

    @Override
    public void data_attributes$addChild(MutableAttribute attribute, AttributeFunction function) {
        if (MutableAttribute.contains(this, attribute)) return;
        attribute.data_attributes$addParent(this, function);
        this.data_attributes$children.put(attribute, function);
    }

    @Unique
    public void data_attributes$clearDescendants() {
        this.data_attributes$parents.clear();
        this.data_attributes$children.clear();
    }

    @Override
    public void data_attributes$clear() {
        this.data_attributes$override(new AttributeOverride(this.data_attributes$enabled, this.defaultValue, this.defaultValue, this.defaultValue, this.defaultValue, 0.0D, StackingFormula.Flat, AttributeFormat.Whole));
        this.data_attributes$clearDescendants();
    }

    @Override
    public Double data_attributes$sum(final double k, final double k2, final double v, final double v2, AttributeInstance instance) {
        return this.data_attributes$formula.result(k, k2, v, v2, instance);
    }

    @Override
    public Map<IAttribute, AttributeFunction> data_attributes$parentsMutable() {
        return this.data_attributes$parents;
    }

    @Override
    public Map<IAttribute, AttributeFunction> data_attributes$childrenMutable() {
        return this.data_attributes$children;
    }

    @Override
    public Double data_attributes$min() { return this.data_attributes$min; }

    @Override
    public Double data_attributes$max() { return this.data_attributes$max; }

    @Override
    public Double data_attributes$min_fallback() {
        return Double.MIN_VALUE;
    }

    @Override
    public Double data_attributes$max_fallback() {
        return Double.MAX_VALUE;
    }

    @Override
    public StackingFormula data_attributes$formula() {
        return this.data_attributes$formula;
    }

    @Override
    public AttributeFormat data_attributes$format() {
        return this.data_attributes$format;
    }

    @Override
    public Map<IAttribute, AttributeFunction> data_attributes$parents() {
        return ImmutableMap.copyOf(this.data_attributes$parents);
    }

    @Override
    public Map<IAttribute, AttributeFunction> data_attributes$children() {
        return ImmutableMap.copyOf(this.data_attributes$children);
    }
}
