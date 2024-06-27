package com.bibireden.data_attributes.mixin;

import java.util.*;
import java.util.function.Consumer;

import com.bibireden.data_attributes.data.AttributeFunction;
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

		double k = 0.0D, v = 0.0D, k2 = 0.0D, v2 = 0.0D;

		if (this.getBaseValue() > 0.0D) {
			k = formula.stack(k, this.getBaseValue());
			k2 = formula.max(k2, this.getBaseValue());
		} else {
			v = formula.stack(v, this.getBaseValue());
			v2 = formula.max(v2, this.getBaseValue());
		}

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.ADDITION)) {
			double value = modifier.getValue();

			if (value > 0.0D) {
				k = formula.stack(k, value);
				k2 = formula.max(k2, value);
			} else {
				v = formula.stack(v, value);
				v2 = formula.max(v2, value);
			}
		}

		if (this.data_containerCallback != null) {
			Map<IEntityAttribute, AttributeFunction> parents = ((MutableEntityAttribute) attribute).data_attributes$parentsMutable();

			for (IEntityAttribute parent : parents.keySet()) {
				EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance((EntityAttribute) parent);

				if (instance == null) continue;
				AttributeFunction function = parents.get(parent);

				if (function.behavior() != StackingBehavior.Add) continue;
				double multiplier = function.value();
				double value = multiplier * instance.getValue();

				if (value > 0.0D) {
					k = formula.stack(k, value);
					// We don't put this here because follow-on attribute values should always be
					// diminishing (if the attribute supports it).
					// k2 = behaviour.max(k2, value);
				} else {
					v = formula.stack(v, value);
					// We don't put this here because follow-on attribute values should always be
					// diminishing (if the attribute supports it).
					// v2 = behaviour.max(v2, value);
				}
			}
		}

		double d = attribute.data_attributes$sum(k, k2, v, v2);
		double e = d;

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_BASE)) {
			e += d * modifier.getValue();
		}

		for (EntityAttributeModifier modifier : this.getModifiersByOperation(EntityAttributeModifier.Operation.MULTIPLY_TOTAL)) {
			e *= 1.0D + modifier.getValue();
		}

		if (this.data_containerCallback != null) {
			Map<IEntityAttribute, AttributeFunction> parents = ((MutableEntityAttribute) attribute).data_attributes$parentsMutable();

			for (IEntityAttribute parent : parents.keySet()) {
				EntityAttributeInstance instance = this.data_containerCallback.getCustomInstance((EntityAttribute) parent);

				if (instance == null) continue;
				AttributeFunction function = parents.get(parent);

				if (function.behavior() != StackingBehavior.Multiply)
					continue;
				e *= 1.0D + (instance.getValue() * function.value());
			}
		}


		if (formula == StackingFormula.Diminished) {
			e = e * (attribute.data_attributes$max() - attribute.data_attributes$min());
			e += attribute.data_attributes$min();
		}

        return ((EntityAttribute) attribute).clamp(e);
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
