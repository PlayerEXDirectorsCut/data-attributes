package com.bibireden.data_attributes.registry

import net.fabricmc.fabric.api.event.registry.DynamicRegistryView
import net.minecraft.util.Identifier

/**
 * Meant to be applied to `DynamicRegistry` handling classes.
 * This should normally be applied to a singleton.
 */
interface DynamicRegistry<D : Any> {
    /** Applies the new `DynamicRegistry` data, reflecting the changes across `DataAttributes`. */
    fun apply()

    /** Whenever an `attribute` `DynamicRegistry` specific entry (file) is added. */
    fun onEntryAdded(rawID: Int, ref: Identifier, obj: D)

    /** Whenever an `attribute` `DynamicRegistry` specific entry (file) is removed. */
    fun onEntryRemoved(rawID: Int, ref: Identifier, obj: D)

    /** Always runs before a dynamic registry is being loaded (meant to clear data and setup events and apply new data). */
    fun onSetupCallback(view: DynamicRegistryView)
}