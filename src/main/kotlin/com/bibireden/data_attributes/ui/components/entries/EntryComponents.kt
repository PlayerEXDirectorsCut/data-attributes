package com.bibireden.data_attributes.ui.components.entries

import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text

object EntryComponents {
    fun double(label: Text, properties: DataEntryComponent.Properties<Double>? = null, suggestion: String? = null, hs: Sizing? = Sizing.fill(100), vs: Sizing? = Sizing.fixed(20)): DataEntryComponent<Double> {
        return DataEntryComponent(label, String::toDoubleOrNull, properties, suggestion, hs, vs)
    }
}