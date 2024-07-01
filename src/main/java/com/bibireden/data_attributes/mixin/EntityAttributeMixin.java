package com.bibireden.data_attributes.mixin;

import java.util.Map;

import com.bibireden.data_attributes.config.models.OverridesConfigModel;
import com.bibireden.data_attributes.config.functions.AttributeFunction;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @Unique protected boolean data_enabled;
    @Unique protected double data_min, data_max, data_smoothness;

    @Final
    @Shadow
    private double fallback;
    
    // Constructor injection to initialize additional fields
    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_attributes$init(String translationKey, double fallback, CallbackInfo ci) {
        this.data_min = Double.MIN_VALUE;
        this.data_max = Double.MAX_VALUE;
        this.data_formula = StackingFormula.Flat;
        this.data_parents = new Object2ObjectArrayMap<>();
        this.data_children = new Object2ObjectArrayMap<>();
    }

    @ModifyReturnValue(method = "isTracked", at = @At("RETURN"))
    private boolean data_attributes$isTracked(boolean original) {
        return true;
    }

    @ModifyReturnValue(method = "clamp", at = @At("RETURN"))
    private double data_attributes$clamp(double original) {
        if (this.data_enabled) {
            return this.data_attributes$clamped(original);
        }

        return original;
    }

    @Unique
    protected double data_attributes$clamped(double valueIn) {
        double value = EntityAttributeModifiedEvents.CLAMPED.invoker().onClamped((EntityAttribute)(Object)this, valueIn);
        return MathHelper.clamp(value, this.data_attributes$min(), this.data_attributes$max());
    }

    @Override
    public void data_attributes$override(OverridesConfigModel.AttributeOverride override) {
        this.data_enabled = override.enabled;
        this.data_min = override.min;
        this.data_max = override.max;
        this.data_smoothness = override.smoothness;
        this.data_formula = override.formula;
    }

    @Override
    public void data_attributes$addParent(MutableEntityAttribute attributeIn, AttributeFunction function) {
        this.data_parents.put(attributeIn, function);
    }

    @Override
    public void data_attributes$addChild(MutableEntityAttribute attributeIn, AttributeFunction function) {
        if(MutableEntityAttribute.contains(this, attributeIn)) return;
        attributeIn.data_attributes$addParent(this, function);
        this.data_children.put(attributeIn, function);
    }

    @Unique
    public void data_attributes$clearDescendants() {
        this.data_parents.clear();
        this.data_children.clear();
    }

    @Override
    public void data_attributes$clear() {
        this.data_attributes$override(new OverridesConfigModel.AttributeOverride(this.data_enabled, this.fallback, this.fallback, this.fallback, this.fallback, 0.0D, StackingFormula.Flat));
        this.data_attributes$clearDescendants();
    }

    @Override
    public double data_attributes$sum(final double k, final double k2, final double v, final double v2) {
        return this.data_formula.result(k, k2, v, v2, this.data_smoothness);
    }

    @Override
    public Map<IEntityAttribute, AttributeFunction> data_attributes$parentsMutable() {
        return this.data_parents;
    }

    @Override
    public Map<IEntityAttribute, AttributeFunction> data_attributes$childrenMutable() {
        return this.data_children;
    }

    @Override
    public double data_attributes$min() { return this.data_min; }

    @Override
    public double data_attributes$max() { return this.data_max; }

    @Override
    public double data_attributes$min_fallback() {
        return Double.MIN_VALUE;
    }

    @Override
    public double data_attributes$max_fallback() {
        return Double.MAX_VALUE;
    }

    @Override
    public StackingFormula data_attributes$formula() {
        return this.data_formula;
    }

    @Override
    public Map<IEntityAttribute, IAttributeFunction> data_attributes$parents() {
        return ImmutableMap.copyOf(this.data_parents);
    }

    @Override
    public Map<IEntityAttribute, IAttributeFunction> data_attributes$children() {
        return ImmutableMap.copyOf(this.data_children);
    }
}
