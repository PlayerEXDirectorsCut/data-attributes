package com.bms.data_attributes.mixin;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.bms.data_attributes.mutable.MutableAttribute;
import com.bms.data_attributes.mutable.MutableAttributeMap;
import com.bms.data_attributes.mutable.MutableAttributeModifier;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.bms.data_attributes.api.attribute.StackingBehavior;
import com.bms.data_attributes.api.attribute.IAttribute;
import com.bms.data_attributes.api.attribute.IAttributeInstance;
import com.bms.data_attributes.api.attribute.StackingFormula;
import com.bms.data_attributes.api.event.AttributeModifiedEvents;
import com.bms.data_attributes.api.util.VoidConsumer;
import com.bms.data_attributes.mutable.MutableAttributeInstance;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttributeInstance.class)
abstract class AttributeInstanceMixin implements MutableAttributeInstance, IAttributeInstance {
	@Unique
	private AttributeMap data_attributes$container;

	@Unique
	private ResourceLocation data_attributes$id;

	@Final
	@Shadow
	private Holder<Attribute> attribute;

	@Final
	@Shadow
	private Map<ResourceLocation, AttributeModifier> modifierById;

	@Shadow
	private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation operation) {
		return Collections.emptySet();
	}

	@Shadow
	protected void setDirty() {}

	@Shadow public abstract double getBaseValue();

	@Shadow @Nullable public abstract AttributeModifier getModifier(ResourceLocation id);

	@Shadow abstract Map<ResourceLocation, AttributeModifier> getModifiers(AttributeModifier.Operation operation);

	@Shadow public abstract Holder<Attribute> getAttribute();

	@Shadow @Final private Map<ResourceLocation, AttributeModifier> permanentModifiers;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void data_attributes$init(Holder<Attribute> attribute, Consumer<AttributeInstance> onDirty, CallbackInfo ci) {
		this.data_attributes$id = BuiltInRegistries.ATTRIBUTE.getKey(this.attribute.value());
	}

	@ModifyReturnValue(method = "getAttribute", at = @At("RETURN"))
	private Holder<Attribute> data_attributes$getAttribute(Holder<Attribute> original) {
		Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(data_attributes$id);
		return attribute != null ? BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute) : original;
	}

	@SuppressWarnings("UnreachableCode")
	@Inject(method = "calculateValue", at = @At("HEAD"), cancellable = true)
	private void data_attributes$calculateValue(CallbackInfoReturnable<Double> cir) {
		Attribute attribute = this.getAttribute().value();
		StackingFormula formula = attribute.data_attributes$formula();

		// If the formula is set to Flat and there is no associated container, drop out early.
		if (formula == StackingFormula.Flat && this.data_attributes$container == null) return;

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

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
			double value = modifier.amount();

			if (value > 0.0D) {
				k.set(formula.stack(k.get(), value));
				k2 = formula.max(k2, value);
			} else {
				v.set(formula.stack(v.get(), value));
				v2 = formula.max(v2, value);
			}
		}

		if (this.data_attributes$container != null) {
			attribute.data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (!function.getEnabled() || function.getBehavior() != StackingBehavior.Add) return;

				AttributeInstance parentInstance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) parentAttribute));
				if (parentInstance == null) return;

				double multiplier = function.getValue();
				double value = multiplier * parentInstance.getValue();

				if (value > 0.0D) {
					k.set(formula.stack(k.get(), value));
				} else {
					v.set(formula.stack(v.get(), value));
				}
			});
		}

		double d = attribute.data_attributes$sum(k.get(), k2, v.get(), v2, (AttributeInstance) (Object) this);
		AtomicReference<Double> e = new AtomicReference<>(d);

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
			e.set(e.get() + (d * modifier.amount()));
		}

		for (AttributeModifier modifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
			e.set(e.get() * (1.0D + modifier.amount()));
		}

		if (this.data_attributes$container != null) {
            attribute.data_attributes$parentsMutable().forEach((parentAttribute, function) -> {
				if (!function.getEnabled() || function.getBehavior() != StackingBehavior.Multiply) return;

				AttributeInstance parentInstance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) parentAttribute));
				if (parentInstance == null) return;

				e.set(e.get() * (1.0D + (parentInstance.getValue() * function.getValue())));
			});
		}

		cir.setReturnValue(attribute.sanitizeValue(e.get()));
	}

	@Inject(method = "addModifier", at = @At("HEAD"), cancellable = true)
	private void data_attributes$addModifier(AttributeModifier modifier, CallbackInfo ci) {
		ResourceLocation key = modifier.id();
		AttributeModifier AttributeModifier = this.modifierById.get(key);

		if (AttributeModifier != null) {
			throw new IllegalArgumentException("Modifier is already applied on this attribute!");
		} else {
			this.data_attributes$actionModifier(
				() -> {
					this.modifierById.put(key, modifier);
					this.getModifiers(modifier.operation()).put(key, modifier);
				}, (AttributeInstance) (Object) this, modifier, true
			);
		}

		ci.cancel();
	}

	@Inject(method = "removeModifier(Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;)V", at = @At("HEAD"), cancellable = true)
	private void data_attributes$removeModifier(AttributeModifier modifier, CallbackInfo ci) {
		this.data_attributes$actionModifier(
			() -> {
				this.getModifiers(modifier.operation()).remove(modifier);
				this.modifierById.remove(modifier.id());
				this.permanentModifiers.remove(modifier);
			},
			(AttributeInstance) (Object) this,
			modifier,
			false
		);

		ci.cancel();
	}

	@WrapOperation(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putString(Ljava/lang/String;Ljava/lang/String;)V", ordinal = 0))
	private void data_attributes$save(CompoundTag instance, String key, String value, Operation<Void> original) {
		String entry;
		if (this.data_attributes$id == null) {
			ResourceLocation location = BuiltInRegistries.ATTRIBUTE.getKey(this.attribute.value());
			if (location != null) {
				entry = location.toString();
			}
			else {
				original.call(instance, key, value);
				return;
			}
		}
		else {
			entry = this.data_attributes$id.toString();
		}
		instance.putString("id", entry);
	}

	@Override
	public ResourceLocation data_attributes$get_id() {
		return this.data_attributes$id;
	}

	@Override
	public void data_attributes$actionModifier(final VoidConsumer consumerIn, final AttributeInstance instanceIn, final AttributeModifier modifierIn, final boolean isWasAdded) {
		if (this.data_attributes$container == null) return;

		Attribute parent = this.getAttribute().value();

		for (IAttribute child : parent.data_attributes$childrenMutable().keySet()) {
			AttributeInstance instance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) child));
			if (instance != null) instance.getValue();
		}

		final double value = instanceIn.getValue();

		consumerIn.accept();

		this.setDirty();

		LivingEntity livingEntity = (this.data_attributes$container).data_attributes$getLivingEntity();

		AttributeModifiedEvents.Modified.stream.sink().onModified(parent, livingEntity, modifierIn, value, isWasAdded);

		for (IAttribute child : parent.data_attributes$childrenMutable().keySet()) {
			AttributeInstance instance = this.data_attributes$container.getInstance(BuiltInRegistries.ATTRIBUTE.wrapAsHolder((Attribute) child));
			if (instance != null) {
				instance.data_attributes$actionModifier(() -> {}, instance, modifierIn, isWasAdded);
			}
		}
	}

	@Override
	public void data_attributes$setContainerCallback(final AttributeMap mapIn) {
		this.data_attributes$container = mapIn;
	}

	@Override
	public void data_attributes$updateId(final ResourceLocation resource) {
		this.data_attributes$id = resource;
	}

	@Override
	public void data_attributes$updateModifier(final ResourceLocation id, final double value) {
		AttributeModifier modifier = this.getModifier(id);
		if (modifier == null) return;

		this.data_attributes$actionModifier(() -> modifier.data_attributes$updateValue(value), (AttributeInstance) (Object) this, modifier, false);
	}

	@Override
	public void data_attributes$refresh() { this.setDirty(); }
}
