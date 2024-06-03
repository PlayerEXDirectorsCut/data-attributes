package com.bibireden.data_attributes.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.bibireden.data_attributes.data.*;
import com.bibireden.data_attributes.endec.NbtDeserializer;
import com.bibireden.data_attributes.endec.NbtSerializer;
import com.google.gson.JsonElement;
import io.wispforest.endec.format.json.JsonDeserializer;
import org.slf4j.Logger;

import com.bibireden.data_attributes.api.DataAttributesAPI;
import com.bibireden.data_attributes.api.event.AttributesReloadedEvent;
import com.bibireden.data_attributes.data.AttributeFunction;
import com.bibireden.data_attributes.mutable.MutableEntityAttribute;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.registry.Registries;

public final class AttributeManager implements SimpleResourceReloadListener<AttributeManager.Wrapper> {
	private static final Gson GSON = (new GsonBuilder()).excludeFieldsWithoutExposeAnnotation().create();
	private static final int PATH_SUFFIX_LENGTH = ".json".length();
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String DIRECTORY = "attributes";
	private static final Identifier ID = new Identifier(DataAttributesAPI.MODID, DIRECTORY);
	private static final Map<Identifier, Tuple<Integer>> ENTITY_TYPE_INSTANCES = new HashMap<>();

	private Map<Identifier, EntityAttributeData> entityAttributeData = ImmutableMap.of();
	private Map<Identifier, EntityTypeData> entityTypeData = ImmutableMap.of();
	private final AttributeContainerHandler handler = new AttributeContainerHandler();
	private int updateFlag = 0;

	protected record Tuple<T>(Class<? extends LivingEntity> livingEntity, T value) {
	}

	protected record Wrapper(Map<Identifier, EntityAttributeData> entityAttributeData, Map<Identifier, EntityTypeData> entityTypeData) {}

	public AttributeManager() {}

	private static Map<Identifier, AttributeFunction> formatFunctions(Map<String, AttributeFunction> functionsIn) {
		Map<Identifier, AttributeFunction> functions = new HashMap<>();

		for (String key : functionsIn.keySet()) {
			AttributeFunction value = functionsIn.get(key);

			functions.put(new Identifier(key), value);
		}

		return functions;
	}

	private static EntityAttribute getOrCreate(final Identifier identifier, EntityAttribute attributeIn) {
		EntityAttribute attribute = Registries.ATTRIBUTE.get(identifier);

		if (attribute == null) {
			attribute = MutableRegistryImpl.register(Registries.ATTRIBUTE, identifier, attributeIn);
		}

		return attribute;
	}

	private static void loadOverrides(ResourceManager manager, Map<Identifier, EntityAttributeData> entityAttributeData) {
		String location = DIRECTORY + "/overrides";
		int length = location.length() + 1;

		Map<Identifier, AttributeOverride> cache = new HashMap<>();

		for (Map.Entry<Identifier, Resource> entry : manager.findResources(location, id -> id.getPath().endsWith(".json")).entrySet()) {
			Identifier resource = entry.getKey();
			String path = resource.getPath();

			Identifier identifier = new Identifier(resource.getNamespace(), path.substring(length, path.length() - PATH_SUFFIX_LENGTH));

            try (BufferedReader reader = entry.getValue().getReader()) {
                AttributeOverride override = AttributeOverride.Companion.getEndec().decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement.class));
                if (cache.put(identifier, override) != null) {
                    LOGGER.error("Overriding override with found duplicate: {}", identifier);
                }
            } catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("Failed to parse data file {} from {} :: {}", identifier, resource, exception);
            }
		}

		cache.forEach((key, value) -> entityAttributeData.put(key, new EntityAttributeData(value)));
	}

	private static void loadFunctions(ResourceManager manager, Map<Identifier, EntityAttributeData> entityAttributeData) {
		Map<Identifier, AttributeFunctions> cache = new HashMap<>();
		int length = DIRECTORY.length() + 1;

		for (Map.Entry<Identifier, Resource> entry : manager.findResources(DIRECTORY, id -> id.getPath().endsWith("functions.json")).entrySet()) {
			Identifier resource = entry.getKey();
			String path = resource.getPath();
			Identifier identifier = new Identifier(resource.getNamespace(), path.substring(length, path.length() - PATH_SUFFIX_LENGTH));

			try(BufferedReader reader = entry.getValue().getReader()) {
				AttributeFunctions json = AttributeFunctions.Companion.getEndec().decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement.class));

				if (cache.put(identifier, json) != null) {
					LOGGER.error("Overriding function(s) with found duplicate: {}", identifier);
				}
			} catch (IOException | IllegalArgumentException exception) {
				LOGGER.error("Failed to parse data file {} from {} :: {}", identifier, resource, exception);
			}
		}

		Map<String, Map<String, AttributeFunction>> functions = new HashMap<>();
		cache.values().forEach(json -> json.merge(functions));

		for (String key : functions.keySet()) {
			Identifier identifier = new Identifier(key);
			EntityAttributeData data = entityAttributeData.getOrDefault(identifier, new EntityAttributeData());
			data.putFunctions(formatFunctions(functions.get(key)));
			entityAttributeData.put(identifier, data);
		}
	}

	private static void loadEntityTypes(ResourceManager manager, Map<Identifier, EntityTypeData> entityTypeData) {
		Map<Identifier, EntityTypes> cache = new HashMap<>();
		int length = DIRECTORY.length() + 1;

		for (Map.Entry<Identifier, Resource> entry : manager.findResources(DIRECTORY, id -> id.getPath().endsWith("entity_types.json")).entrySet()) {
			Identifier resource = entry.getKey();
			String path = resource.getPath();
			Identifier identifier = new Identifier(resource.getNamespace(), path.substring(length, path.length() - PATH_SUFFIX_LENGTH));

			try(BufferedReader reader = entry.getValue().getReader()) {
				EntityTypes types = EntityTypes.Companion.getEndec().decodeFully(JsonDeserializer::of, GSON.fromJson(reader, JsonElement.class));
				if (cache.put(identifier, types) != null) {
					LOGGER.error("Overriding entity-types with found duplicate: {}", identifier);
				}
			}
			catch (IOException | IllegalArgumentException exception) {
				LOGGER.error("Failed to parse data file {} from {} :: {}", identifier, resource, exception);
			}
		}

		Map<String, Map<String, Double>> entityTypes = new HashMap<>();
		cache.values().forEach(json -> json.merge(entityTypes));

		for (String key : entityTypes.keySet()) {
			Identifier identifier = new Identifier(key);
			EntityTypeData data = new EntityTypeData(entityTypes.get(key));
			entityTypeData.put(identifier, data);
		}
	}

	public void toNbt(NbtCompound tag) {
		NbtCompound entityAttributeNbt = new NbtCompound();
		NbtCompound entityTypeNbt = new NbtCompound();

		this.entityAttributeData.forEach((key, val) -> {
			entityAttributeNbt.put(key.toString(), EntityAttributeData.Companion.getEndec().encodeFully(NbtSerializer::of, val));
		});

		this.entityTypeData.forEach((key, val) -> {
			NbtCompound entry = new NbtCompound();
			val.writeToNbt(entry);
			entityTypeNbt.put(key.toString(), entry);
		});

		tag.put("Attributes", entityAttributeNbt);
		tag.put("EntityTypes", entityTypeNbt);
		tag.putInt("UpdateFlag", this.updateFlag);
	}

	public void fromNbt(NbtCompound tag) {
		if (tag.contains("Attributes")) {
			ImmutableMap.Builder<Identifier, EntityAttributeData> builder = ImmutableMap.builder();
			NbtCompound nbtCompound = tag.getCompound("Attributes");
			nbtCompound.getKeys().forEach(key -> {
				NbtCompound entry = nbtCompound.getCompound(key);
				EntityAttributeData entityAttributeData = EntityAttributeData.Companion.getEndec().decodeFully(NbtDeserializer::of, entry);
				builder.put(new Identifier(key), entityAttributeData);
			});
			this.entityAttributeData = builder.build();
		}

		if (tag.contains("EntityTypes")) {
			ImmutableMap.Builder<Identifier, EntityTypeData> builder = ImmutableMap.builder();
			NbtCompound nbtCompound = tag.getCompound("EntityTypes");
			nbtCompound.getKeys().forEach(key -> {
				NbtCompound entry = nbtCompound.getCompound(key);
				EntityTypeData entityTypeData = new EntityTypeData();
				entityTypeData.readFromNbt(entry);
				builder.put(new Identifier(key), entityTypeData);
			});
			this.entityTypeData = builder.build();
		}
		this.updateFlag = tag.getInt("UpdateFlag");
	}

	public void nextUpdateFlag() {
		this.updateFlag++;
	}

	public int getUpdateFlag() {
		return this.updateFlag;
	}

	public AttributeContainer getContainer(final EntityType<? extends LivingEntity> entityType, final LivingEntity livingEntity) {
		return this.handler.getContainer(entityType, livingEntity);
	}

	public void apply() {
		MutableRegistryImpl.unregister(Registries.ATTRIBUTE);

		for (Identifier identifier : Registries.ATTRIBUTE.getIds()) {
			EntityAttribute entityAttribute = Registries.ATTRIBUTE.get(identifier);

			if (entityAttribute == null)
				continue;

			((MutableEntityAttribute) entityAttribute).clear();
		}

		for (Identifier identifier : this.entityAttributeData.keySet()) {
			EntityAttributeData entityAttributeData = this.entityAttributeData.get(identifier);
			entityAttributeData.override(identifier, AttributeManager::getOrCreate);
		}

		for (Identifier identifier : this.entityAttributeData.keySet()) {
			EntityAttribute entityAttribute = Registries.ATTRIBUTE.get(identifier);

			if (entityAttribute == null)
				continue;

			EntityAttributeData entityAttributeData = this.entityAttributeData.get(identifier);
			entityAttributeData.copy(entityAttribute);
		}

		this.handler.buildContainers(this.entityTypeData, ENTITY_TYPE_INSTANCES);

		AttributesReloadedEvent.EVENT.invoker().onCompletedReload();
	}

	@Override
	public CompletableFuture<Wrapper> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, EntityAttributeData> entityAttributeData = new HashMap<>();
			loadOverrides(manager, entityAttributeData);
			loadFunctions(manager, entityAttributeData);

			Map<Identifier, EntityTypeData> entityTypeData = new HashMap<>();
			loadEntityTypes(manager, entityTypeData);

			return new Wrapper(entityAttributeData, entityTypeData);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(Wrapper data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			ImmutableMap.Builder<Identifier, EntityAttributeData> entityAttributeData = ImmutableMap.builder();
			data.entityAttributeData.forEach(entityAttributeData::put);
			this.entityAttributeData = entityAttributeData.build();

			ImmutableMap.Builder<Identifier, EntityTypeData> entityTypeData = ImmutableMap.builder();
			data.entityTypeData.forEach(entityTypeData::put);
			this.entityTypeData = entityTypeData.build();

			this.apply();
		}, executor);
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	static {
		ENTITY_TYPE_INSTANCES.put(
				new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_LIVING_ENTITY),
				new Tuple<>(LivingEntity.class, 0));
		ENTITY_TYPE_INSTANCES.put(new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_MOB_ENTITY),
				new Tuple<>(MobEntity.class, 1));
		ENTITY_TYPE_INSTANCES.put(
				new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_PATH_AWARE_ENTITY),
				new Tuple<>(PathAwareEntity.class, 2));
		ENTITY_TYPE_INSTANCES.put(
				new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_HOSTILE_ENTITY),
				new Tuple<>(HostileEntity.class, 3));
		ENTITY_TYPE_INSTANCES.put(
				new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_PASSIVE_ENTITY),
				new Tuple<>(PassiveEntity.class, 4));
		ENTITY_TYPE_INSTANCES.put(
				new Identifier(DataAttributesAPI.MODID, DataAttributesAPI.ENTITY_INSTANCE_ANIMAL_ENTITY),
				new Tuple<>(AnimalEntity.class, 5));
	}
}