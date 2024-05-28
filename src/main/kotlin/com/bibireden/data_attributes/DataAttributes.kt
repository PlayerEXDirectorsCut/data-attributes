package com.bibireden.data_attributes

import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.registry.DynamicRegistries
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class DataAttributes : ModInitializer {
    companion object {
        const val MOD_ID = "data-attributes"

        val logger = LoggerFactory.getLogger(MOD_ID)

        object Keys {
            val Attributes: RegistryKey<Registry<Map<Identifier, Dynamic<*>>>> = RegistryKey.ofRegistry(Identifier("attributes"))
        }
    }

    override fun onInitialize() {
        DynamicRegistries.registerSynced(Keys.Attributes, Codec.unboundedMap(Identifier.CODEC, Codec.PASSTHROUGH), DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY)

        DynamicRegistrySetupCallback.EVENT.register { view ->
            view.registerEntryAdded(Keys.Attributes) { _, id, encoded ->

            }
        }
    }
}