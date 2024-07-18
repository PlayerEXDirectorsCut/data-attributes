package com.bibireden.data_attributes.mixin;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bibireden.data_attributes.api.attribute.StackingBehavior;
import com.bibireden.data_attributes.api.attribute.IEntityAttribute;
import com.bibireden.data_attributes.api.attribute.IEntityAttributeInstance;
import com.bibireden.data_attributes.api.attribute.StackingFormula;
import com.bibireden.data_attributes.api.event.EntityAttributeModifiedEvents;
import com.bibireden.data_attributes.api.util.VoidConsumer;
import com.bibireden.data_attributes.mutable.MutableAttributeContainer;
import com.bibireden.data_attributes.mutable.MutableAttributeInstance;
import com.bibireden.data_attributes.mutable.MutableAttributeModifier;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

@Mixin(EntityAttributeInstance.class)
abstract class EntityAttributeInstanceMixin implements MutableAttributeInstance, IEntityAttributeInstance {

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
	private Collection<EntityAttributeModifier> getModifiersByOperation(EntityAttributeModifier.Operation operation) {
		return Collections.emptySet();
	}

	@Shadow
	protected void onUpdate() {}

	@Shadow public abstract double getBaseValue();

	@Shadow public abstract EntityAttribute getAttribute();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_init(EntityAttribute type, Consumer<EntityAttributeInstance> updateCallback, CallbackInfo ci) {
		this.data_identifier = Registries.ATTRIBUTE.getId(type);
	}

	@ModifyReturnValue(method = "getAttribute", at = @At("RETURN"))
	private EntityAttribute data_getAttribute(EntityAttribute original) {
		EntityAttribute attribute = Registries.ATTRIBUTE.get(this.data_identifier);
		return attribute != null ? attribute : original;
	}

	@ModifyReturnValue(method = "computeValue", at = @At("RETURN"))
	private double data_attributes$computeValue(double original) {
		MutableEntityAttribute attribute = (MutableEntityAttribute) this.getAttribute();
		StackingFormula formula = attribute.data_attributes$formula();

		// If the formula is set to Flat and there is no associated container, provide original.
		if (formula == StackingFormula.Flat && this.data_containerCallback == null) return original;

		AtomicReference<Double> k = new AtomicReference<>(0.0D);
		AtomicReference<Double> v = new AtomicReference<>(0.0D);

		double k2 = 0.0D;
		double v2 = 0.0D;

		if (this.getBaseValue() > 0.0D) {
			k.set(formula.stack(k.get(), this.getBaseValue()));
			k2 = formula.max(k2, this.getBaseValue());
		} else {
			v.set(formula.stack(v.get(), this.getBaseValue()));
			v2 = formula.max(v2, this.getBaseValue());
		}

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
			double value = modifier.getValue();

			if (value > 0.0D) {
				k.set(formula.stack(k.get(), value));
				k2 = formula.max(k2, value);
			} else {
				v.set(formula.stack(v.get(), value));
				v2 = formula.max(v2, value);
			}
		}

		if (this.data_containerCallback != null) {
			attribute.data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (function.behavior() != StackingBehavior.Add) return;

				EntityAttributeInstance parentInstance = this.data_containerCallback.getCustomInstance((EntityAttribute) parentAttribute);
				if (parentInstance == null) return;

				double multiplier = function.value();
				double value = multiplier * parentInstance.getValue();

				if (value > 0.0D) {
					k.set(formula.stack(k.get(), value));
				} else {
					v.set(formula.stack(v.get(), value));
				}
			});
		}

		double d = attribute.data_attributes$sum(k.get(), k2, v.get(), v2);
		AtomicReference<Double> e = new AtomicReference<>(d);

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
			e.set(e.get() + (d * modifier.getValue()));
		}

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
			e.set(e.get() * (1.0D + modifier.getValue()));
		}

		if (this.data_containerCallback != null) {
            attribute.data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (function.behavior() != StackingBehavior.Multiply) return;

				EntityAttributeInstance parentInstance = this.data_containerCallback.getCustomInstance((EntityAttribute) parentAttribute);
				if (parentInstance == null) return;

				e.set(e.get() * (1.0D + (parentInstance.getValue() * function.value())));
			});
		}

        return ((EntityAttribute) attribute).clamp(e.get());
	}

	@Inject(method = "addModifier", at = @At("HEAD"), cancellable = true)
	private void data_addModifier(EntityAttributeModifier modifier, CallbackInfo ci) {
		EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;
		UUID key = modifier.getId();
		EntityAttributeModifier entityAttributeModifier = (EntityAttributeModifier) this.idToModifiers.get(key);

		if (entityAttributeModifier != null) {
			throw new IllegalArgumentException("Modifier is already applied on this attribute!");
		} else {
			this.data_attributes$actionModifier(() -> {
				this.idToModifiers.put(key, modifier);
				instance.getModifiers(modifier.getOperation()).add(modifier);
			}, instance, modifier, true);
		}

		ci.cancel();
	}

	@Inject(method = "removeModifier(Lnet/minecraft/entity/attribute/EntityAttributeModifier;)V", at = @At("HEAD"), cancellable = true)
	private void data_attributes$removeModifier(EntityAttributeModifier modifier, CallbackInfo ci) {
		EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;

		this.data_attributes$actionModifier(() -> {
			instance.getModifiers(modifier.getOperation()).remove(modifier);
			this.idToModifiers.remove(modifier.getId());
			this.persistentModifiers.remove(modifier);
		}, instance, modifier, false);

		ci.cancel();
	}

	@ModifyExpressionValue(method = "toNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
	private Identifier data_attributes$toNbt(Identifier id) {
		if (this.data_identifier == null)
			return Registries.ATTRIBUTE.getId(this.type);
		return this.data_identifier;
	}

	@Override
	public Identifier data_attributes$get_id() {
		return this.data_identifier;
	}

	@Override
	public void data_attributes$actionModifier(final VoidConsumer consumerIn, final EntityAttributeInstance instanceIn, final EntityAttributeModifier modifierIn, final boolean isWasAdded) {
		EntityAttribute entityAttribute = ((EntityAttributeInstance) (Object) this).getAttribute();
		MutableEntityAttribute parent = (MutableEntityAttribute) entityAttribute;

		if (this.data_containerCallback == null)
			return;
		for (IEntityAttribute child : parent.data_attributes$childrenMutable().keySet()) {
			EntityAttribute attribute = (EntityAttribute) child;
			EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance(attribute);

			if (instance != null) {
				instance.getValue();
			}
		}

		final double value = instanceIn.getValue();

		consumerIn.accept();

		this.onUpdate();

		LivingEntity livingEntity = ((MutableAttributeContainer) this.data_containerCallback).data_attributes$getLivingEntity();

		EntityAttributeModifiedEvents.MODIFIED.invoker().onModified(entityAttribute, livingEntity, modifierIn, value, isWasAdded);

		for (IEntityAttribute child : parent.data_attributes$childrenMutable().keySet()) {
			EntityAttribute attribute = (EntityAttribute) child;
			EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance(attribute);

			if (instance != null) {
				((MutableAttributeInstance) instance).data_attributes$actionModifier(() -> {
				}, instance, modifierIn, isWasAdded);
			}
		}
	}

	@Override
	public void data_attributes$setContainerCallback(final AttributeContainer containerIn) {
		this.data_containerCallback = containerIn;
	}

	@Override
	public void data_attributes$updateId(final Identifier identifierIn) {
		this.data_identifier = identifierIn;
	}

	@Override
	public void updateModifier(final UUID uuid, final double value) {
		EntityAttributeInstance instance = (EntityAttributeInstance) (Object) this;
		EntityAttributeModifier modifier = instance.getModifier(uuid);

		if (modifier == null)
			return;

		this.data_attributes$actionModifier(() -> {
			((MutableAttributeModifier) modifier).data_attributes$updateValue(value);
		}, instance, modifier, false);
	}

	@Override
	public void data_attributes$refresh() {
		this.onUpdate();
	}
}
