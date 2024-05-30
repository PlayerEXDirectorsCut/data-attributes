package com.bibireden.data_attributes.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.bibireden.data_attributes.json.AttributeFunctionJson;
import com.bibireden.data_attributes.json.AttributeOverrideJson;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

public final class EntityAttributeData implements NbtIO {
	private AttributeOverrideJson attribute;
	private final Map<Identifier, AttributeFunctionJson> functions;

	public EntityAttributeData() {
		this.functions = new HashMap<>();
	}

	public EntityAttributeData(final AttributeOverrideJson attribute) {
		this();
		this.attribute = attribute;
	}

	public void override(final Identifier identifier, BiFunction<Identifier, EntityAttribute, EntityAttribute> function) {
		if (this.attribute == null) return;
		EntityAttribute entityAttribute = function.apply(identifier, this.attribute.create());
		this.attribute.override((MutableEntityAttribute) entityAttribute);
	}

	public void copy(EntityAttribute entityAttributeIn) {
		MutableEntityAttribute mutableEntityAttribute = (MutableEntityAttribute) entityAttributeIn;

		for (Identifier identifier : this.functions.keySet()) {
			EntityAttribute entityAttribute = Registries.ATTRIBUTE.get(identifier);

			if (entityAttribute == null) continue;

			AttributeFunctionJson function = this.functions.get(identifier);
			mutableEntityAttribute.addChild((MutableEntityAttribute) entityAttribute, function);
		}
	}

	public void putFunctions(Map<Identifier, AttributeFunctionJson> functions) {
		this.functions.putAll(functions);
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		if (tag.contains("Attribute")) {
			this.attribute = new AttributeOverrideJson();
			this.attribute.readFromNbt(tag.getCompound("Attribute"));
		}

		NbtCompound functions = tag.getCompound("Functions");
		functions.getKeys().forEach(key -> this.functions.put(new Identifier(key), AttributeFunctionJson.read(functions.getByteArray(key))));
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtCompound attribute = new NbtCompound();

		if (this.attribute != null) {
			this.attribute.writeToNbt(attribute);
			tag.put("Attribute", attribute);
		}

		NbtCompound functions = new NbtCompound();
		this.functions.forEach((key, value) -> functions.putByteArray(key.toString(), value.write()));
		tag.put("Functions", functions);
	}
}
