package com.bibireden.data_attributes.mixin;

import java.util.*;
import java.util.function.Consumer;

import com.bibireden.data_attributes.api.attribute.DynamicEntityAttribute;
import com.bibireden.data_attributes.api.attribute.DynamicEntityAttributeInstance;
import com.bibireden.data_attributes.api.enums.StackingFormula;
import com.bibireden.data_attributes.api.events.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.mutable.MutableAttributeContainer;
import com.bibireden.data_attributes.mutable.MutableAttributeInstance;
import com.bibireden.data_attributes.mutable.MutableAttributeModifier;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;
import com.bibireden.data_attributes.registry.AttributeSkillData;
import com.bibireden.data_attributes.utils.DiminishingMathKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

@Mixin(EntityAttributeInstance.class)
abstract class EntityAttributeInstanceMixin implements MutableAttributeInstance, DynamicEntityAttributeInstance {

    @Unique
    private AttributeContainer data_containerCallback;

    @Unique
    private Identifier data_identifier;

    @Final
    @Shadow
    private EntityAttribute type;

    @Final
    @Shadow
    private Map<UUID, EntityAttributeModifier> idToModifiers;

    @Final
    @Shadow
    private Set<EntityAttributeModifier> persistentModifiers;

    @Shadow
    private double baseValue;

    @Shadow
    private Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation) {
        return Collections.emptySet();
    }

    @Shadow
    protected void onUpdate() {
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void data_init(EntityAttribute type, Consumer<EntityAttributeInstance> updateCallback, CallbackInfo ci) {
        this.data_identifier = Registries.ATTRIBUTE.getId(type);
    }

    @Inject(method = "getAttribute", at = @At("HEAD"), cancellable = true)
    private void data_getAttribute(CallbackInfoReturnable<EntityAttribute> ci) {
        EntityAttribute attribute = Registries.ATTRIBUTE.get(this.data_identifier);

        if (attribute != null) {
            ci.setReturnValue(attribute);
        } else {
            ci.setReturnValue(this.type);
        }
    }

    @Inject(method = "computeValue", at = @At("HEAD"), cancellable = true)
    private void data_computeValue(CallbackInfoReturnable<Double> ci) {
        MutableEntityAttribute attribute = (MutableEntityAttribute) ((EntityAttributeInstance) (Object) this).getAttribute();

        StackingFormula formula = attribute.formula();

        double addOperationResult;
        {
            Set<Double> positives = new HashSet<>();
            Set<Double> negatives = new HashSet<>();

            for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
                double value = modifier.getValue();
                if (value >= 0.0D) {
                    positives.add(value);
                } else {
                    negatives.add(value);
                }
            }
            addOperationResult = DiminishingMathKt.calculateStacking(positives, negatives, null, attribute.behavior(), formula);
        }


        if (this.data_containerCallback != null) {
            Map<DynamicEntityAttribute, AttributeSkillData> parents = ((MutableEntityAttribute) attribute).parentsMutable();

            Set<Double> positiveAdded = new HashSet<>();
            Set<Double> negativeAdded = new HashSet<>();

            for (Map.Entry<DynamicEntityAttribute, AttributeSkillData> entry : parents.entrySet()) {
                DynamicEntityAttribute parent = entry.getKey();
                AttributeSkillData function = entry.getValue();

                EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance((EntityAttribute) parent);
                if (instance == null) continue;

                double value = function.value() * instance.getValue();

                if (value > 0.0D)
                {
                    positiveAdded.add(value);
                } else if (value < 0.0D) {
                    negativeAdded.add(value);
                }
            }

            addOperationResult += DiminishingMathKt.calculateStacking(positiveAdded, negativeAdded, null, attribute.behavior(), formula);
        }

        double e = addOperationResult;

        for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
            e += addOperationResult * modifier.getValue();
        }

        for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
            e *= 1.0D + modifier.getValue();
        }

        double value = ((EntityAttribute) attribute).clamp(e);
        ci.setReturnValue(value);
    }

    @Inject(method = "addModifier", at = @At("HEAD"), cancellable = true)
    private void data_addModifier(EntityAttributeModifier modifier, CallbackInfo ci) {
        EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;
        UUID key = modifier.getId();
        EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier) this.idToModifiers.get(key);

        if (entityAttributeModifier != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.actionModifier(() -> {
                this.idToModifiers.put(key, modifier);
                instance.getModifiers(modifier.getOperation()).add(modifier);
                return null;
            }, instance, modifier, true);
        }

        ci.cancel();
    }

    @Inject(method = "removeModifier", at = @At("HEAD"), cancellable = true)
    private void data_removeModifier(EntityAttributeModifier modifier, CallbackInfo ci) {
        EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;

        this.actionModifier(() -> {
            instance.getModifiers(modifier.getOperation()).remove(modifier);
            this.idToModifiers.remove(modifier.getId());
            this.persistentModifiers.remove(modifier);
            return null;
        }, instance, modifier, false);

        ci.cancel();
    }

    @Redirect(method = "toNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private Identifier data_toNbt(Registry<?> registry, Object type) {
        if (this.data_identifier == null)
            return Registries.ATTRIBUTE.getId((EntityAttribute) type);
        return this.data_identifier;
    }

    @Override
    public Identifier id() {
        return this.data_identifier;
    }

    @Override
    public void actionModifier(@NotNull Function0<Unit> func, @NotNull EntityAttributeInstance instanceIn, @NotNull EntityAttributeModifier modifierIn, boolean isAdded) {
        EntityAttribute entityAttribute = ((EntityAttributeInstance) (Object) this).getAttribute();
        MutableEntityAttribute parent = (MutableEntityAttribute) entityAttribute;

        if (this.data_containerCallback == null)
            return;
        for (DynamicEntityAttribute child : parent.childrenMutable().keySet()) {
            EntityAttribute attribute = (EntityAttribute) child;
            EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance(attribute);

            if (instance != null) {
                instance.getValue();
            }
        }

        final double value = instanceIn.getValue();

        func.invoke();

        this.onUpdate();

        LivingEntity livingEntity = ((MutableAttributeContainer) this.data_containerCallback).getLivingEntity();

        EntityAttributeModifiedEvents.Companion.getMODIFIED().invoker().onModified(entityAttribute, livingEntity, modifierIn, value, isAdded);

        for (DynamicEntityAttribute child : parent.childrenMutable().keySet()) {
            EntityAttribute attribute = (EntityAttribute) child;
            EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance(attribute);

            if (instance != null) {
                ((MutableAttributeInstance) instance).actionModifier(() -> null, instance, modifierIn, isAdded);
            }
        }
    }

    @Override
    public void setContainerCallback(final AttributeContainer containerIn) {
        this.data_containerCallback = containerIn;
    }

    @Override
    public void updateId(final Identifier identifierIn) {
        this.data_identifier = identifierIn;
    }

    @Override
    public void updateModifier(final UUID uuid, final double value) {
        EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;
        EntityAttributeModifier modifier = instance.getModifier(uuid);

        if (modifier == null)
            return;

        this.actionModifier(() -> {
            ((MutableAttributeModifier) modifier).update(value);
            return null;
        }, instance, modifier, false);
    }

    @Override
    public void refresh() {
        this.onUpdate();
    }
}