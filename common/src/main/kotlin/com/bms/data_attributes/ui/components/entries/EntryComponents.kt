package com.bms.data_attributes.ui.components.entries

import io.wispforest.owo.ui.core.Sizing
import net.minecraft.network.chat.Component

object EntryComponents {
    fun double(label: Component, properties: DataEntryComponent.Properties<Double>? = null, suggestion: String? = null, hs: Sizing? = Sizing.fill(100), vs: Sizing? = Sizing.fixed(20)): DataEntryComponent<Double> {
        return DataEntryComponent(label, String::toDoubleOrNull, properties, suggestion, hs, vs)
    }
}