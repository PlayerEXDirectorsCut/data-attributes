package com.bibireden.data_attributes

import com.bibireden.data_attributes.registry.AttributeDynamicRegistry
import com.mojang.serialization.Codec
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data-attributes"

        val logger = LoggerFactory.getLogger(MOD_ID)

        val registry = AttributeDynamicRegistry() // prepare the masses, singletons are making a comeback!
    }

    override fun onInitialize() {
        DynamicRegistrySetupCallback.EVENT.register(registry::onSetupCallback)
        
        DynamicRegistries.registerSynced(AttributeDynamicRegistry.key, Codec.unboundedMap(Identifier.CODEC, Codec.PASSTHROUGH), DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)
    }
}