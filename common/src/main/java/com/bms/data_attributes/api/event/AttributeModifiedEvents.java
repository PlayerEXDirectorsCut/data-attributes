package com.bms.data_attributes.api.event;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Holds some attribute events. Both occur on both server and client.
 * 
 * @author CleverNucleus
 *
 */
public final class AttributeModifiedEvents {
	
	/**
	 * Fired when the value of an attribute instance was modified, either by adding/removing a modifier or changing 
	 * the value of the modifier, or by reloading the datapack and having the living entity renew its attribute container. 
	 * Living entity and modifiers may or may not be null.
	 */
	public static final Event<Modified> MODIFIED = EventFactory.createArrayBacked(Modified.class, callbacks -> (attribute, livingEntity, modifier, prevValue, isWasAdded) -> {
		for(Modified callback : callbacks) {
			callback.onModified(attribute, livingEntity, modifier, prevValue, isWasAdded);
		}
	});
	
	/**
	 * Fired after the attribute instance value was calculated, but before it was output. This offers one last chance to alter the 
	 * value in some way (for example, rounding a decimal to an integer).
	 */
	public static final Event<Clamped> CLAMPED = EventFactory.createArrayBacked(Clamped.class, callbacks -> (attribute, value) -> {
		double cache = value;
		
		for(Clamped callback : callbacks) {
			cache = callback.onClamped(attribute, cache);
		}
		
		return cache;
	});
	
	@FunctionalInterface
	public interface Modified {
		void onModified(final Attribute attribute, final @Nullable LivingEntity livingEntity, final @Nullable AttributeModifier modifier, final double prevValue, final boolean isWasAdded);
	}
	
	@FunctionalInterface
	public interface Clamped {
		double onClamped(final Attribute attribute, final double value);
	}
}
