package com.bibireden.data_attributes.registry

import com.bibireden.data_attributes.DataAttributes
import com.mojang.serialization.Dynamic
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

/**
 * Used to be the internal handler for objects obtained from the `attribute` `DynamicRegistry`.
 *
 * It manages changes to the `attributes` registry, and reflects these changes to the rest of `DataAttributes`.
 *
 * It also has the job of being a singleton instance which can be instantiated containing the relevant data from the `DynamicRegistries` upon a signal for reload.
 */
class AttributeDynamicRegistry : DynamicRegistry<AttributeRegistryEntry> {
    companion object {
        val key: RegistryKey<Registry<AttributeRegistryEntry>> = RegistryKey.ofRegistry(Identifier("attributes"))
    }

    override fun onSetupCallback(view: DynamicRegistryView)
    {
        DataAttributes.logger.info("onSetupCallback called :: {}", view.stream().toList().size)
        view.registerEntryAdded(key, this::onEntryAdded)
        view.registerEntryRemoved(key, this::onEntryRemoved)
    }

    override fun onEntryAdded(rawID: Int, ref: Identifier, obj: AttributeRegistryEntry) {
        DataAttributes.logger.info("onEntryAdded called :: ID#:{}, ref#:{}, data#:{}", rawID, ref, obj)

        this.apply()
    }

    override fun onEntryRemoved(rawID: Int, ref: Identifier, obj: AttributeRegistryEntry) {
        DataAttributes.logger.info("onEntryRemoved called :: ID#:{}, ref:{}, data?:{}", rawID, ref, obj)

        this.apply()
    }

    override fun apply() {
        DataAttributes.logger.info("Applied :: Dynamic Registry")
    }
}

/** Quick alias for data that is meant to be deserialized. */
private typealias AttributeRegistryEntry = Map<Identifier, Dynamic<*>>