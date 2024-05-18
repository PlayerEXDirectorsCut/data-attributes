package com.bibireden.data_attributes

import com.bibireden.data_attributes.registry.AttributeData
import com.bibireden.data_attributes.registry.AttributeOverrideData
import com.bibireden.data_attributes.registry.AttributeRegistryKeys
import com.mojang.serialization.Codec
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class DataAttributes : ModInitializer {
    companion object {
        val MOD_ID = "data_attributes"
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
    }

    override fun onInitialize() {
        DynamicRegistries.registerSynced(AttributeRegistryKeys.SKILLS, Codec.unboundedMap(Identifier.CODEC, AttributeOverrideData.CODEC), DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)
        DynamicRegistries.registerSynced(AttributeRegistryKeys.ENTITIES, AttributeData.CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)
        DynamicRegistries.registerSynced(AttributeRegistryKeys.OVERRIDES, AttributeData.CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)
    }
}